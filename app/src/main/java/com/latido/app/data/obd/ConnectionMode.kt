package com.latido.app.data.obd

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Where the car data comes from. */
enum class ConnectionMode {
    /** Simulated ELM327 (replay) — works on the emulator, no hardware. */
    DEMO,

    /** A real ELM327 over Bluetooth (Classic or BLE, auto-detected). */
    BLUETOOTH
}

/** Steps the connection goes through, surfaced as friendly status while loading. */
enum class ConnectionPhase { IDLE, SCANNING, CONNECTING, CONNECTED, FAILED }

/** App-wide, in-memory selection of the connection mode. Defaults to demo so it never breaks on
 *  a device without an adapter (e.g. the emulator). */
@Singleton
class ConnectionModeStore @Inject constructor() {
    private val _mode = MutableStateFlow(ConnectionMode.DEMO)
    val mode: StateFlow<ConnectionMode> = _mode.asStateFlow()

    fun set(mode: ConnectionMode) {
        _mode.value = mode
    }
}
