package com.latido.app.domain.model

/**
 * Context about the vehicle that is sent to the diagnosis provider along with the DTCs.
 * In Phase 1 this is mocked; in Phase 2 it is filled from the real VIN / freeze frame.
 */
data class VehicleContext(
    val vin: String?,
    val make: String?,
    val model: String?,
    val year: Int?,
    /** Snapshot of values at the time of the fault (freeze frame). Empty until Phase 2. */
    val freezeFrame: Map<String, String> = emptyMap()
) {
    val hasIdentity: Boolean
        get() = !make.isNullOrBlank() || !model.isNullOrBlank() || !vin.isNullOrBlank()
}
