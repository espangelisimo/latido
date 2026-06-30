package com.latido.app.data.obd

import com.latido.app.domain.ObdReading
import com.latido.app.domain.VehicleRepository
import com.latido.app.domain.model.VehicleContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the car through the real ELM327 pipeline. The [ObdConnectionManager] decides whether the
 * transport underneath is the replay (demo) or a real Bluetooth adapter; this class is identical
 * either way.
 */
@Singleton
class ObdVehicleRepository @Inject constructor(
    private val connectionManager: ObdConnectionManager,
    private val modeStore: ConnectionModeStore
) : VehicleRepository {

    override val isDemo: Boolean
        get() = modeStore.mode.value == ConnectionMode.DEMO

    override suspend fun read(): ObdReading {
        val client = connectionManager.acquireClient()
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
        val client = connectionManager.acquireClient()
        return client.clearDtcs()
    }
}
