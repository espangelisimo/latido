package com.latido.app.data.mock

import com.latido.app.domain.ObdReading
import com.latido.app.domain.VehicleRepository
import com.latido.app.domain.model.VehicleContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simulated car. Returns a realistic scenario (misfire + catalyst) with a mock VIN and
 * identity. "Clearing" the codes empties the list so the read -> clear flow can be demoed.
 */
@Singleton
class MockVehicleRepository @Inject constructor() : VehicleRepository {

    override val isDemo: Boolean = true

    private var cleared = false

    override suspend fun read(): ObdReading {
        delay(900)
        val vehicle = VehicleContext(
            vin = "VF1RFB00X12345678",
            make = "Renault",
            model = "Mégane",
            year = 2016,
            freezeFrame = mapOf(
                "rpm" to "820",
                "engine_temp_c" to "91",
                "speed_kmh" to "0"
            )
        )
        return if (cleared) {
            ObdReading(vehicle = vehicle)
        } else {
            ObdReading(
                vehicle = vehicle,
                // Mixed severities so the colour coding is visible: green + amber + red.
                storedCodes = listOf("P0300", "P0455", "P0335"),
                pendingCodes = listOf("P0171")
            )
        }
    }

    override suspend fun clearCodes(): Boolean {
        delay(700)
        cleared = true
        return true
    }
}
