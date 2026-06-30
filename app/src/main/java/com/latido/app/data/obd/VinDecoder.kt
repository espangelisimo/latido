package com.latido.app.data.obd

import java.util.Locale

/** Best-effort identity derived from a VIN: manufacturer (from the WMI) and model year. */
data class VinIdentity(
    val make: String?,
    val model: String?,
    val year: Int?
)

/**
 * Lightweight VIN decoder. Resolves the manufacturer from the 3-char World Manufacturer
 * Identifier and the model year from the 10th character. Model is left to the LLM / later work,
 * as it cannot be derived reliably from the VIN alone.
 */
object VinDecoder {

    // A small WMI table covering common makes (mostly European). Extend as needed.
    private val WMI: Map<String, String> = mapOf(
        "VF1" to "Renault", "VF2" to "Renault", "VF7" to "Citroën", "VF3" to "Peugeot",
        "VF6" to "Renault Trucks", "UU1" to "Dacia", "UU6" to "Dacia",
        "WVW" to "Volkswagen", "WV1" to "Volkswagen", "WV2" to "Volkswagen",
        "WAU" to "Audi", "WUA" to "Audi", "TRU" to "Audi",
        "WBA" to "BMW", "WBS" to "BMW", "WBY" to "BMW",
        "WDB" to "Mercedes-Benz", "WDD" to "Mercedes-Benz", "WDC" to "Mercedes-Benz",
        "W1K" to "Mercedes-Benz", "WME" to "Smart",
        "WP0" to "Porsche", "WP1" to "Porsche",
        "ZFA" to "Fiat", "ZFF" to "Ferrari", "ZAR" to "Alfa Romeo", "ZLA" to "Lancia",
        "VSS" to "SEAT", "TMB" to "Škoda", "W0L" to "Opel", "W0V" to "Opel",
        "SJN" to "Nissan", "SB1" to "Toyota", "VNK" to "Toyota", "VR1" to "DS",
        "VR3" to "Peugeot", "VR7" to "Citroën", "MA3" to "Suzuki"
    )

    // Model-year codes (position 10). Letters/digits I,O,Q,U,Z,0 are never used.
    private val YEAR_CODES: Map<Char, Int> = buildMap {
        val letters = "ABCDEFGHJKLMNPRSTVWXY"
        letters.forEachIndexed { i, c -> put(c, 2010 + i) }
        // Earlier cycle digits 1..9 -> 2001..2009 (assume recent vehicles).
        for (d in 1..9) put('0' + d, 2000 + d)
    }

    fun decode(vin: String?): VinIdentity {
        if (vin == null || vin.length != 17) return VinIdentity(null, null, null)
        val v = vin.uppercase(Locale.ROOT)
        val make = WMI[v.substring(0, 3)]
        val year = YEAR_CODES[v[9]]
        return VinIdentity(make = make, model = null, year = year)
    }
}
