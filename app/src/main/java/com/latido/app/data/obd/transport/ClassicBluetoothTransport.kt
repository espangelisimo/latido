package com.latido.app.data.obd.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.latido.app.data.obd.ObdException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * RFCOMM / SPP transport for Bluetooth Classic ELM327 dongles. Uses the standard Serial Port
 * Profile UUID, with the well-known reflection fallback for clones whose secure/insecure socket
 * connect fails.
 */
@SuppressLint("MissingPermission")
class ClassicBluetoothTransport(
    private val device: BluetoothDevice,
    private val adapter: BluetoothAdapter?
) : ObdTransport {

    override val name: String get() = device.name ?: device.address
    override val type: TransportType = TransportType.CLASSIC

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private val buffer = ByteArray(1024)

    override val isConnected: Boolean get() = socket?.isConnected == true

    override suspend fun connect() = withContext(Dispatchers.IO) {
        // Scanning slows down / breaks an RFCOMM connect.
        runCatching { adapter?.cancelDiscovery() }

        val s = openSocket()
        try {
            s.connect()
        } catch (e: Exception) {
            runCatching { s.close() }
            // Reflection fallback: some clones only accept channel 1 directly.
            val fallback = reflectionSocket()
            if (fallback != null) {
                runCatching { fallback.connect() }
                    .onFailure {
                        runCatching { fallback.close() }
                        throw ObdException.ConnectionFailed(e.message ?: "RFCOMM connect failed")
                    }
                bind(fallback)
                return@withContext
            }
            throw ObdException.ConnectionFailed(e.message ?: "RFCOMM connect failed")
        }
        bind(s)
    }

    private fun openSocket(): BluetoothSocket =
        runCatching { device.createRfcommSocketToServiceRecord(SPP_UUID) }
            .getOrElse { device.createInsecureRfcommSocketToServiceRecord(SPP_UUID) }

    private fun reflectionSocket(): BluetoothSocket? = runCatching {
        val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
        method.invoke(device, 1) as BluetoothSocket
    }.getOrNull()

    private fun bind(s: BluetoothSocket) {
        socket = s
        input = s.inputStream
        output = s.outputStream
    }

    override suspend fun write(bytes: ByteArray) = withContext(Dispatchers.IO) {
        val out = output ?: throw ObdException.ConnectionFailed("not connected")
        out.write(bytes)
        out.flush()
    }

    override suspend fun read(): ByteArray = withContext(Dispatchers.IO) {
        val inp = input ?: throw ObdException.ConnectionFailed("not connected")
        val count = inp.read(buffer)
        if (count <= 0) ByteArray(0) else buffer.copyOf(count)
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        runCatching { input?.close() }
        runCatching { output?.close() }
        runCatching { socket?.close() }
        socket = null
        input = null
        output = null
    }

    private companion object {
        // Serial Port Profile UUID.
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
