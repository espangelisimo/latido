package com.latido.app.ui

import com.latido.app.domain.model.DiagnosisResult
import com.latido.app.domain.model.Dtc
import com.latido.app.domain.model.VehicleContext

/** Mock readiness verdict for the inspection (ITV/MOT/…) card. Real logic arrives in Phase 5. */
enum class InspectionStatus { PASS, WARN, FAIL, UNKNOWN }

/** State of reading the car (free, no AI). */
sealed interface ReadState {
    data object Idle : ReadState
    data object Loading : ReadState
    data object Loaded : ReadState
    data object Error : ReadState
}

enum class DiagnosisErrorType { NETWORK, GENERIC }

/** State of the AI verdict (Pro). Locked until the user explicitly runs it. */
sealed interface DiagnosisUiState {
    data object Locked : DiagnosisUiState
    data object Loading : DiagnosisUiState
    data class Success(val result: DiagnosisResult) : DiagnosisUiState
    data class Error(val type: DiagnosisErrorType) : DiagnosisUiState
}

data class CarUiState(
    val isDemo: Boolean = true,
    val readState: ReadState = ReadState.Idle,
    val vehicle: VehicleContext? = null,
    val storedCodes: List<Dtc> = emptyList(),
    val pendingCodes: List<Dtc> = emptyList(),
    val isClearing: Boolean = false,
    val diagnosis: DiagnosisUiState = DiagnosisUiState.Locked,
    val inspection: InspectionStatus = InspectionStatus.UNKNOWN
) {
    val hasCodes: Boolean get() = storedCodes.isNotEmpty() || pendingCodes.isNotEmpty()
    val isConnected: Boolean get() = readState == ReadState.Loaded
}
