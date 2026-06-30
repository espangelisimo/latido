package com.latido.app.di

import com.latido.app.data.mock.MockDiagnosisProvider
import com.latido.app.data.obd.ObdVehicleRepository
import com.latido.app.data.obd.transport.ObdTransport
import com.latido.app.data.obd.transport.ReplayObdTransport
import com.latido.app.domain.DiagnosisProvider
import com.latido.app.domain.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Wires the swappable abstractions to their current implementations.
 *  - Diagnosis: still the mock (the real LLM proxy lands in Phase 3).
 *  - Vehicle data: the real OBD pipeline (Elm327Client) over the replay transport (Phase 2A).
 *    Phase 2B swaps the transport binding to the Bluetooth ones.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDiagnosisProvider(impl: MockDiagnosisProvider): DiagnosisProvider

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: ObdVehicleRepository): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindObdTransport(impl: ReplayObdTransport): ObdTransport
}
