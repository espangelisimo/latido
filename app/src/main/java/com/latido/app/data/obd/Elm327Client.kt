package com.latido.app.data.obd

import com.latido.app.data.obd.transport.ObdTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Speaks the ELM327 protocol over an [ObdTransport]. All socket I/O runs on [Dispatchers.IO];
 * every command has a read timeout. Initialisation is run once and guarded so repeated reads
 * don't re-handshake.
 *
 * Created per connection by [com.latido.app.data.obd.ObdConnectionManager] with the chosen
 * transport (Classic, BLE or the replay), so the client itself is transport-agnostic.
 */
class Elm327Client(
    private val transport: ObdTransport
) {
    private val io = Dispatchers.IO
    private var initialized = false

    fun isConnected(): Boolean = transport.isConnected

    /** Connect the transport (if needed) and run the AT init sequence once. */
    suspend fun connectAndInit() = withContext(io) {
        if (!transport.isConnected) transport.connect()
        if (initialized) return@withContext
        // ATZ reset, ATE0 echo off, ATL0 linefeeds off, ATS0 spaces off, ATH0 headers off,
        // ATSP0 auto protocol. (ATH1 only if header parsing is needed.)
        listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0").forEach { sendCommand(it) }
        initialized = true
    }

    suspend fun readStoredDtcs(): List<String> =
        ObdResponseParser.parseDtcs(sendCommand("03"), serviceByte = 0x43)

    suspend fun readPendingDtcs(): List<String> =
        ObdResponseParser.parseDtcs(sendCommand("07"), serviceByte = 0x47)

    suspend fun readVin(): String? =
        ObdResponseParser.parseVin(sendCommand("0902", timeoutMs = 8000))

    /** Best-effort freeze-frame snapshot (Mode 02). Unsupported PIDs are simply skipped. */
    suspend fun readFreezeFrame(): Map<String, String> {
        val out = linkedMapOf<String, String>()
        ObdResponseParser.parsePidData(sendCommand("020C00"), 0x42, 0x0C)?.let { d ->
            if (d.size >= 2) out["rpm"] = (((d[0] * 256) + d[1]) / 4).toString()
        }
        ObdResponseParser.parsePidData(sendCommand("020500"), 0x42, 0x05)?.let { d ->
            if (d.isNotEmpty()) out["coolant_c"] = (d[0] - 40).toString()
        }
        ObdResponseParser.parsePidData(sendCommand("020D00"), 0x42, 0x0D)?.let { d ->
            if (d.isNotEmpty()) out["speed_kmh"] = d[0].toString()
        }
        ObdResponseParser.parsePidData(sendCommand("020400"), 0x42, 0x04)?.let { d ->
            if (d.isNotEmpty()) out["engine_load_pct"] = (d[0] * 100 / 255).toString()
        }
        return out
    }

    /** Clear stored DTCs (Mode 04). Returns true on a positive 0x44 response. */
    suspend fun clearDtcs(): Boolean {
        val response = sendCommand("04")
        val clean = ObdResponseParser.clean(response)
        return !clean.isError && clean.bytes.contains(0x44)
    }

    /** Send a command and read until the `>` prompt. Throws [ObdException.ReadTimeout] on stall. */
    suspend fun sendCommand(command: String, timeoutMs: Long = 5000): String = withContext(io) {
        transport.write((command + "\r").toByteArray(Charsets.US_ASCII))
        val sb = StringBuilder()
        try {
            withTimeout(timeoutMs) {
                while (true) {
                    val chunk = transport.read()
                    if (chunk.isNotEmpty()) {
                        sb.append(String(chunk, Charsets.US_ASCII))
                        if (sb.contains('>')) break
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw ObdException.ReadTimeout(command)
        }
        sb.toString().replace(">", "").trim()
    }

    suspend fun close() {
        initialized = false
        transport.close()
    }
}
