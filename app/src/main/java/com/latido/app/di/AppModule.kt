package com.latido.app.di

import com.latido.app.data.mock.MockDiagnosisProvider
import com.latido.app.data.mock.MockVehicleRepository
import com.latido.app.domain.DiagnosisProvider
import com.latido.app.domain.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Wires the swappable abstractions to their Phase 1 mock implementations. Later phases
 * change only these bindings (LLM provider, real OBD repository) — the rest is untouched.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDiagnosisProvider(impl: MockDiagnosisProvider): DiagnosisProvider

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: MockVehicleRepository): VehicleRepository
}
