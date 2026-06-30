package com.latido.app.domain.model

/** Whether a code is currently stored (Mode 03) or pending/intermittent (Mode 07). */
enum class DtcStatus { STORED, PENDING }

/**
 * A diagnostic trouble code as read from the car, enriched with the local dictionary
 * for the free basic view (short name + severity colour). The AI never decides these.
 */
data class Dtc(
    /** Normalised code, e.g. "P0300". Always compared/built using Locale.ROOT. */
    val code: String,
    val status: DtcStatus = DtcStatus.STORED,
    /** Localised short name from the DTC dictionary, or null if unknown. */
    val name: String? = null,
    val severity: Severity = Severity.UNKNOWN,
    val safetyCritical: Boolean = false
)
