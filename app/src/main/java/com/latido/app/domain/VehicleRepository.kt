package com.latido.app.domain

import com.latido.app.domain.model.VehicleContext

/** Raw outcome of reading the car: identity plus the codes (still un-enriched). */
data class ObdReading(
    val vehicle: VehicleContext,
    val storedCodes: List<String> = emptyList(),
    val pendingCodes: List<String> = emptyList()
)

/**
 * Source of vehicle data. Phase 1 uses a mock; Phase 2 replaces it with real ELM327 reads
 * over Bluetooth, without touching the UI.
 */
interface VehicleRepository {
    suspend fun read(): ObdReading

    /** Clear stored faults in the car (Mode 04). Free, never gated behind Pro. */
    suspend fun clearCodes(): Boolean

    /** True when the source is simulated data (shown as a "demo" hint in the UI). */
    val isDemo: Boolean
}
