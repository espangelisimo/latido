package com.latido.app.data.obd.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import com.latido.app.data.obd.ObdException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

/**
 * GATT / UART transport for BLE ELM327 modules. Wraps the callback-based BLE API in coroutines:
 * connect + discover services + enable notifications happen during [connect]; incoming
 * notifications are funnelled into a channel that [read] consumes.
 *
 * Characteristic discovery prefers the common UART services (Nordic UART, FFF0, FFE0) and falls
 * back to picking any writable + notifiable characteristics.
 */
@SuppressLint("MissingPermission")
class BleBluetoothTransport(
    private val context: Context,
    private val device: BluetoothDevice
) : ObdTransport {

    override val name: String get() = device.name ?: device.address
    override val type: TransportType = TransportType.BLE

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null

    private val inbox = Channel<ByteArray>(Channel.UNLIMITED)
    private val ready = CompletableDeferred<Boolean>()

    @Volatile
    private var connected = false
    override val isConnected: Boolean get() = connected

    private val callback = object : android.bluetooth.BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connected = false
                if (!ready.isCompleted) ready.complete(false)
                inbox.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                if (!ready.isCompleted) ready.complete(false)
                return
            }
            val pair = pickCharacteristics(gatt)
            if (pair == null) {
                if (!ready.isCompleted) ready.complete(false)
                return
            }
            writeChar = pair.first
            notifyChar = pair.second
            enableNotifications(gatt, pair.second)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            connected = true
            if (!ready.isCompleted) ready.complete(true)
        }

        // API < 33
        @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            characteristic.value?.let { inbox.trySend(it) }
        }

        // API >= 33
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            inbox.trySend(value)
        }
    }

    override suspend fun connect() {
        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        val ok = withTimeoutOrNull(CONNECT_TIMEOUT_MS) { ready.await() } ?: false
        if (!ok) {
            close()
            throw ObdException.ConnectionFailed("BLE connect/discovery failed")
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val cccd = characteristic.getDescriptor(CCCD_UUID)
        if (cccd == null) {
            // No CCCD: assume notifications are implicitly on.
            connected = true
            if (!ready.isCompleted) ready.complete(true)
            return
        }
        val value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(cccd, value)
        } else {
            @Suppress("DEPRECATION")
            cccd.value = value
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(cccd)
        }
    }

    override suspend fun write(bytes: ByteArray) {
        val g = gatt ?: throw ObdException.ConnectionFailed("not connected")
        val ch = writeChar ?: throw ObdException.ConnectionFailed("no write characteristic")
        val writeType = if (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        } else {
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(ch, bytes, writeType)
        } else {
            @Suppress("DEPRECATION")
            run {
                ch.writeType = writeType
                ch.value = bytes
                g.writeCharacteristic(ch)
            }
        }
    }

    override suspend fun read(): ByteArray =
        inbox.receiveCatching().getOrNull() ?: ByteArray(0)

    override suspend fun close() {
        connected = false
        runCatching { gatt?.disconnect() }
        runCatching { gatt?.close() }
        gatt = null
        runCatching { inbox.close() }
    }

    /** Choose (writeChar, notifyChar), preferring known UART services. */
    private fun pickCharacteristics(
        gatt: BluetoothGatt
    ): Pair<BluetoothGattCharacteristic, BluetoothGattCharacteristic>? {
        // Preferred UART service/characteristic triples (service, write, notify).
        for ((svc, w, n) in PREFERRED) {
            val service = gatt.getService(svc) ?: continue
            val write = service.getCharacteristic(w) ?: continue
            val notify = service.getCharacteristic(n) ?: continue
            return write to notify
        }
        // Fallback: any writable + any notifiable characteristic across all services.
        var write: BluetoothGattCharacteristic? = null
        var notify: BluetoothGattCharacteristic? = null
        gatt.services.forEach { service ->
            service.characteristics.forEach { c ->
                val p = c.properties
                if (write == null && p and (BluetoothGattCharacteristic.PROPERTY_WRITE or
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                    write = c
                }
                if (notify == null && p and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                        BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                    notify = c
                }
            }
        }
        val w = write ?: return null
        val n = notify ?: return null
        return w to n
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 12000L
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        fun uuid(short: String): UUID = UUID.fromString("0000$short-0000-1000-8000-00805f9b34fb")

        // (service, writeChar, notifyChar)
        val PREFERRED: List<Triple<UUID, UUID, UUID>> = listOf(
            // Nordic UART Service
            Triple(
                UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
                UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"),
                UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
            ),
            // Common clone: FFF0 service, FFF2 write, FFF1 notify
            Triple(uuid("fff0"), uuid("fff2"), uuid("fff1")),
            // HM-10 style: FFE0 service, FFE1 read/write/notify (same char)
            Triple(uuid("ffe0"), uuid("ffe1"), uuid("ffe1"))
        )
    }
}
