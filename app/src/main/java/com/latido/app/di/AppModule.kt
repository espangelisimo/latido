package com.latido.app.di

import com.latido.app.data.mock.MockDiagnosisProvider
import com.latido.app.data.obd.ObdVehicleRepository
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
 *  - Vehicle data: the real OBD pipeline. The transport (demo replay vs Bluetooth) is chosen at
 *    runtime by ObdConnectionManager based on the selected connection mode.
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
}
