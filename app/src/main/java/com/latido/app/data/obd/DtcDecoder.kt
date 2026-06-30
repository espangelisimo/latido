package com.latido.app.data.obd

import java.util.Locale

/**
 * Decodes a 2-byte DTC into its textual form (e.g. 0x03 0x00 -> "P0300").
 *
 * Layout of the first byte (A) and second byte (B), per SAE J2012 / ISO 15031-6:
 *  - A7..A6 -> system letter: 00=P (powertrain), 01=C (chassis), 10=B (body), 11=U (network)
 *  - A5..A4 -> first digit (0..3)
 *  - A3..A0 -> second digit (hex 0..F)
 *  - B7..B4 -> third digit (hex)
 *  - B3..B0 -> fourth digit (hex)
 */
object DtcDecoder {

    private val letters = charArrayOf('P', 'C', 'B', 'U')
    private const val HEX = "0123456789ABCDEF"

    /** Decode a single DTC from two raw bytes (0..255). */
    fun decode(a: Int, b: Int): String {
        val letter = letters[(a shr 6) and 0x03]
        val d1 = (a shr 4) and 0x03
        val d2 = a and 0x0F
        val d3 = (b shr 4) and 0x0F
        val d4 = b and 0x0F
        return buildString {
            append(letter)
            append(d1)
            append(HEX[d2])
            append(HEX[d3])
            append(HEX[d4])
        }.uppercase(Locale.ROOT)
    }

    /** True when both bytes are zero, which is padding rather than a real code. */
    fun isPadding(a: Int, b: Int): Boolean = a == 0 && b == 0
}
