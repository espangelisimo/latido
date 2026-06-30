package com.latido.app.data.obd

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.latido.app.data.bluetooth.Elm327Scanner
import com.latido.app.data.obd.transport.BleBluetoothTransport
import com.latido.app.data.obd.transport.ClassicBluetoothTransport
import com.latido.app.data.obd.transport.ObdTransport
import com.latido.app.data.obd.transport.ReplayObdTransport
import com.latido.app.data.obd.transport.TransportType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the live connection. Picks demo vs Bluetooth from [ConnectionModeStore], scans + auto-
 * detects the adapter type, builds the right transport, and hands back a ready [Elm327Client].
 * Reuses an existing connection across reads so we don't re-scan every time.
 */
@Singleton
class ObdConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: Elm327Scanner,
    private val replayTransport: ReplayObdTransport,
    private val modeStore: ConnectionModeStore
) {
    private val _phase = MutableStateFlow(ConnectionPhase.IDLE)
    val phase: StateFlow<ConnectionPhase> = _phase.asStateFlow()

    private var activeClient: Elm327Client? = null
    private var activeMode: ConnectionMode? = null

    private val adapter: BluetoothAdapter?
        get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    /** Connect (if needed) for the current mode and return an initialised client. */
    suspend fun acquireClient(): Elm327Client {
        val mode = modeStore.mode.value

        activeClient?.let { existing ->
            if (mode == activeMode && existing.isConnected()) return existing
        }
        disconnect()

        try {
            val transport = buildTransport(mode)
            val client = Elm327Client(transport)
            client.connectAndInit()
            activeClient = client
            activeMode = mode
            _phase.value = ConnectionPhase.CONNECTED
            return client
        } catch (e: Throwable) {
            _phase.value = ConnectionPhase.FAILED
            throw e
        }
    }

    private suspend fun buildTransport(mode: ConnectionMode): ObdTransport = when (mode) {
        ConnectionMode.DEMO -> {
            _phase.value = ConnectionPhase.CONNECTING
            replayTransport
        }
        ConnectionMode.BLUETOOTH -> {
            _phase.value = ConnectionPhase.SCANNING
            val found = scanner.findElm327()
            _phase.value = ConnectionPhase.CONNECTING
            when (found.transportType) {
                TransportType.CLASSIC -> ClassicBluetoothTransport(found.device, adapter)
                TransportType.BLE -> BleBluetoothTransport(context, found.device)
                TransportType.REPLAY -> replayTransport
            }
        }
    }

    suspend fun disconnect() {
        activeClient?.let { runCatching { it.close() } }
        activeClient = null
        activeMode = null
        _phase.value = ConnectionPhase.IDLE
    }
}
