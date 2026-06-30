package com.latido.app.data.obd

import com.latido.app.domain.ObdReading
import com.latido.app.domain.VehicleRepository
import com.latido.app.domain.model.VehicleContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the car through the real ELM327 pipeline. In Phase 2A the underlying transport is the
 * replay (so it runs on the emulator); in Phase 2B the same code reads over Bluetooth. Marked as
 * demo while the transport is the replay.
 */
@Singleton
class ObdVehicleRepository @Inject constructor(
    private val client: Elm327Client
) : VehicleRepository {

    override val isDemo: Boolean = true // Replay transport. Becomes false with real Bluetooth.

    override suspend fun read(): ObdReading {
        client.connectAndInit()
        val stored = client.readStoredDtcs()
        val pending = client.readPendingDtcs()
        val vin = client.readVin()
        val freezeFrame = client.readFreezeFrame()
        val identity = VinDecoder.decode(vin)
        return ObdReading(
            vehicle = VehicleContext(
                vin = vin,
                make = identity.make,
                model = identity.model,
                year = identity.year,
                freezeFrame = freezeFrame
            ),
            storedCodes = stored,
            pendingCodes = pending
        )
    }

    override suspend fun clearCodes(): Boolean {
        client.connectAndInit()
        return client.clearDtcs()
    }
}
