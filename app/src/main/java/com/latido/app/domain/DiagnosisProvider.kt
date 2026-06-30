package com.latido.app.domain

import com.latido.app.domain.model.DiagnosisResult
import com.latido.app.domain.model.Dtc
import com.latido.app.domain.model.VehicleContext

/**
 * Single abstraction the whole app talks to for diagnosis. Keeps the UI agnostic of where
 * the result comes from. Implementations:
 *  - MockDiagnosisProvider (Phase 1)
 *  - LlmDiagnosisProvider via the Cloudflare proxy (Phase 3)
 */
interface DiagnosisProvider {
    suspend fun diagnose(context: VehicleContext, dtcs: List<Dtc>): DiagnosisResult
}
