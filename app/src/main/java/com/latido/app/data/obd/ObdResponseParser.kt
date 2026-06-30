package com.latido.app.data.obd

import java.util.Locale

/**
 * Turns the raw text an ELM327 returns into structured data. Tolerant of the messy reality:
 * echoed prompts, `SEARCHING...`, `NO DATA`, `?`, optional spaces (ATS0/ATS1), optional CAN
 * headers (ATH0/ATH1) and ISO-TP multi-frame responses with `0:` / `1:` line prefixes.
 *
 * Pure Kotlin, no Android dependencies, so it is fully unit-tested.
 */
object ObdResponseParser {

    /** Tokens that mean "no usable data" rather than bytes. */
    private val ERROR_TOKENS = setOf(
        "NO DATA", "UNABLE TO CONNECT", "STOPPED", "ERROR", "?", "BUS INIT: ERROR",
        "CAN ERROR", "BUS ERROR", "DATA ERROR", "BUFFER FULL", "FB ERROR", "ACT ALERT",
        "BUS BUSY"
    )

    /** Noise lines that should simply be dropped (not treated as errors). */
    private val NOISE_PREFIXES = listOf("SEARCHING", "BUS INIT", "BUS:")

    private val multiframeLine = Regex("^([0-9A-F]+)\\s*:\\s*(.*)$")
    private val canHeader = Regex("^7E[0-9A-F]\\b")

    data class Clean(val isError: Boolean, val bytes: List<Int>)

    /**
     * Normalise a raw response into a flat list of bytes. Returns [Clean.isError] when the
     * adapter reported a no-data / error condition.
     */
    fun clean(raw: String): Clean {
        val lines = raw
            .split('\r', '\n')
            .map { it.trim().uppercase(Locale.ROOT) }
            .filter { it.isNotEmpty() && it != ">" }

        if (lines.any { line -> ERROR_TOKENS.any { line == it || line.startsWith(it) } }) {
            return Clean(isError = true, bytes = emptyList())
        }

        val dataLines = lines.filterNot { line -> NOISE_PREFIXES.any { line.startsWith(it) } }
        if (dataLines.isEmpty()) return Clean(isError = true, bytes = emptyList())

        val isMultiframe = dataLines.any { multiframeLine.matches(it) }
        val hex = if (isMultiframe) {
            // Keep only `N:` data segments; the bare leading line is the total length, skip it.
            dataLines.mapNotNull { multiframeLine.find(it)?.groupValues?.get(2) }
                .joinToString(" ")
        } else {
            dataLines.joinToString(" ") { stripHeader(it) }
        }

        val cleanedHex = hex.filter { it.isDigit() || it in 'A'..'F' }
        val bytes = cleanedHex.chunked(2).filter { it.length == 2 }.map { it.toInt(16) }
        return Clean(isError = bytes.isEmpty(), bytes = bytes)
    }

    /** Drop a leading CAN header token (e.g. "7E8") when headers are on (ATH1). */
    private fun stripHeader(line: String): String {
        val token = line.substringBefore(' ')
        return if (canHeader.containsMatchIn(token)) line.substringAfter(' ', "") else line
    }

    /**
     * Parse DTCs for a positive-response service byte (0x43 for Mode 03 stored, 0x47 for Mode 07
     * pending). Skips a leading count byte when present and ignores 0x0000 padding.
     */
    fun parseDtcs(raw: String, serviceByte: Int): List<String> {
        val clean = clean(raw)
        if (clean.isError) return emptyList()

        val start = clean.bytes.indexOf(serviceByte)
        if (start < 0) return emptyList()
        var data = clean.bytes.drop(start + 1)

        // CAN replies often prepend a DTC-count byte; an odd remainder reveals it.
        if (data.size % 2 == 1) data = data.drop(1)

        return data.chunked(2)
            .filter { it.size == 2 && !DtcDecoder.isPadding(it[0], it[1]) }
            .map { DtcDecoder.decode(it[0], it[1]) }
            .distinct()
    }

    /**
     * Parse the VIN from a Mode 09 PID 02 response. Layout after cleaning: 0x49 0x02 [count]
     * then 17 ASCII bytes.
     */
    fun parseVin(raw: String): String? {
        val clean = clean(raw)
        if (clean.isError) return null
        val bytes = clean.bytes

        var i = 0
        while (i < bytes.size - 1) {
            if (bytes[i] == 0x49 && bytes[i + 1] == 0x02) break
            i++
        }
        if (i >= bytes.size - 1) return null

        // Skip service (0x49), PID (0x02) and the message-count byte.
        val asciiStart = i + 3
        val ascii = bytes.drop(asciiStart)
            .takeWhile { it in 0x20..0x7E }
            .map { it.toChar() }
            .joinToString("")
            .trim()

        return ascii.takeIf { it.length == 17 && it.all { c -> c.isLetterOrDigit() } }
    }

    /**
     * Return the data bytes following a Mode 0x register response and its PID. Used for live /
     * freeze-frame PIDs (e.g. service 0x42, PID 0x0C). Returns null on error or if not found.
     */
    fun parsePidData(raw: String, responseService: Int, pid: Int): List<Int>? {
        val clean = clean(raw)
        if (clean.isError) return null
        val bytes = clean.bytes

        var i = 0
        while (i < bytes.size - 1) {
            if (bytes[i] == responseService && bytes[i + 1] == pid) {
                return bytes.drop(i + 2)
            }
            i++
        }
        return null
    }
}
