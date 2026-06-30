package com.latido.app.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.latido.app.data.obd.ObdException
import com.latido.app.data.obd.transport.TransportType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Finds an ELM327 adapter, auto-detecting Classic vs BLE.
 *  - Classic: looks at already-bonded (paired) devices, which is how SPP dongles are normally set
 *    up (pair once in Android settings).
 *  - BLE: does a short scan and matches by advertised name.
 *
 * Callers must hold the Bluetooth runtime permissions (checked in the UI). SecurityExceptions are
 * still caught and surfaced as [ObdException.PermissionDenied].
 */
@Singleton
class Elm327Scanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val adapter: BluetoothAdapter?
        get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    fun isBluetoothOn(): Boolean = adapter?.isEnabled == true

    /** Find the first ELM327, preferring an already-paired Classic device, then a BLE scan. */
    @SuppressLint("MissingPermission")
    suspend fun findElm327(bleScanMillis: Long = 6000): DiscoveredDevice {
        val a = adapter ?: throw ObdException.NoAdapterFound()
        if (!a.isEnabled) throw ObdException.BluetoothOff()

        try {
            bondedElm327(a)?.let { return it }
            bleScan(a, bleScanMillis)?.let { return it }
        } catch (e: SecurityException) {
            throw ObdException.PermissionDenied()
        }
        throw ObdException.NoAdapterFound()
    }

    @SuppressLint("MissingPermission")
    private fun bondedElm327(adapter: BluetoothAdapter): DiscoveredDevice? =
        adapter.bondedDevices
            ?.firstOrNull { Elm327Names.looksLikeElm327(it.name) }
            ?.let { DiscoveredDevice(it, it.name, it.address, TransportType.CLASSIC) }

    @SuppressLint("MissingPermission")
    private suspend fun bleScan(adapter: BluetoothAdapter, scanMillis: Long): DiscoveredDevice? {
        val scanner = adapter.bluetoothLeScanner ?: return null
        return withTimeoutOrNull(scanMillis) {
            suspendCancellableCoroutine<DiscoveredDevice?> { cont ->
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                val callback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        val device = result.device ?: return
                        val name = device.name ?: result.scanRecord?.deviceName
                        if (Elm327Names.looksLikeElm327(name) && cont.isActive) {
                            runCatching { scanner.stopScan(this) }
                            cont.resume(
                                DiscoveredDevice(device, name, device.address, TransportType.BLE)
                            )
                        }
                    }

                    override fun onScanFailed(errorCode: Int) {
                        if (cont.isActive) cont.resume(null)
                    }
                }

                cont.invokeOnCancellation { runCatching { scanner.stopScan(callback) } }
                scanner.startScan(null, settings, callback)
            }
        }
    }
}
