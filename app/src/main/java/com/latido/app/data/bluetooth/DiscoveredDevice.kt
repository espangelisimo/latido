package com.latido.app.data.bluetooth

import android.bluetooth.BluetoothDevice
import com.latido.app.data.obd.transport.TransportType

/** A Bluetooth device found during scanning, tagged with how we'd talk to it. */
data class DiscoveredDevice(
    val device: BluetoothDevice,
    val name: String?,
    val address: String,
    val transportType: TransportType
)

/** Name fragments that identify an ELM327-style OBD adapter. Matched case-insensitively. */
object Elm327Names {
    private val PATTERNS = listOf(
        "OBDII", "OBD II", "OBD2", "OBD-II", "ELM327", "ELM", "VIECAR", "VGATE", "VEEPEAK",
        "OBDLINK", "KONNWEI", "VLINK", "V-LINK", "CARISTA", "IOS-VLINK", "SCANTOOL", "OBD"
    )

    fun looksLikeElm327(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val upper = name.uppercase()
        return PATTERNS.any { upper.contains(it) }
    }
}
