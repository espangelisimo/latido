package com.latido.app.data.obd.transport

import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A fake ELM327 that returns realistic, byte-accurate responses for each command, so the whole
 * OBD pipeline (client + parser + decoders + UI) runs end-to-end on the emulator without
 * hardware. Replaced by the real Bluetooth transports in Phase 2B.
 *
 * Simulated car: VIN VF1RFB000G1234567 (Renault, 2016), stored P0300/P0455/P0335, pending P0171.
 */
@Singleton
class ReplayObdTransport @Inject constructor() : ObdTransport {

    override val name: String = "Demo ELM327 (replay)"
    override val type: TransportType = TransportType.REPLAY

    private var connected = false
    override val isConnected: Boolean get() = connected

    private val inbox = Channel<ByteArray>(Channel.UNLIMITED)

    override suspend fun connect() {
        connected = true
    }

    override suspend fun write(bytes: ByteArray) {
        val command = String(bytes, Charsets.US_ASCII).trim().uppercase()
        inbox.send(responseFor(command).toByteArray(Charsets.US_ASCII))
    }

    override suspend fun read(): ByteArray = inbox.receive()

    override suspend fun close() {
        connected = false
    }

    private fun responseFor(command: String): String = when (command) {
        "ATZ" -> reply("ELM327 v1.5")
        "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0" -> reply("OK")
        // Mode 03 stored DTCs: P0300, P0455, P0335
        "03" -> reply("43 03 00 04 55 03 35")
        // Mode 07 pending DTCs: P0171
        "07" -> reply("47 01 71")
        // Mode 09 PID 02 VIN, ISO-TP multi-frame
        "0902" -> reply(
            "014",
            "0: 49 02 01 56 46 31",
            "1: 52 46 42 30 30 30 47",
            "2: 31 32 33 34 35 36 37"
        )
        // Mode 04 clear: positive response
        "04" -> reply("44")
        // Freeze-frame PIDs (Mode 02)
        "020C00" -> reply("42 0C 0C D0") // RPM 820
        "020500" -> reply("42 05 83")    // coolant 91 C
        "020D00" -> reply("42 0D 00")    // speed 0 km/h
        "020400" -> reply("42 04 33")    // engine load ~20 %
        else -> reply("NO DATA")
    }

    /** Join lines with CR and terminate with the ELM327 prompt, like a real adapter. */
    private fun reply(vararg lines: String): String =
        lines.joinToString("\r") + "\r\r>"
}
