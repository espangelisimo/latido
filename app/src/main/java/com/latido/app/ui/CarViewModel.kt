package com.latido.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latido.app.data.dtc.DtcDictionary
import com.latido.app.domain.DiagnosisProvider
import com.latido.app.domain.VehicleRepository
import com.latido.app.domain.model.Dtc
import com.latido.app.domain.model.DtcStatus
import com.latido.app.domain.model.VehicleContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val diagnosisProvider: DiagnosisProvider,
    private val dictionary: DtcDictionary
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarUiState(isDemo = vehicleRepository.isDemo))
    val uiState: StateFlow<CarUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    /** Read the car (free): populates the basic code list. Does NOT call the AI. */
    fun analyze() {
        if (_uiState.value.readState == ReadState.Loading) return
        _uiState.update {
            it.copy(readState = ReadState.Loading, diagnosis = DiagnosisUiState.Locked)
        }
        viewModelScope.launch {
            runCatching { vehicleRepository.read() }
                .onSuccess { reading ->
                    val stored = dictionary.enrichAll(reading.storedCodes, DtcStatus.STORED)
                    val pending = dictionary.enrichAll(reading.pendingCodes, DtcStatus.PENDING)
                    _uiState.update {
                        it.copy(
                            readState = ReadState.Loaded,
                            vehicle = reading.vehicle,
                            storedCodes = stored,
                            pendingCodes = pending,
                            inspection = mockInspection(stored.isNotEmpty() || pending.isNotEmpty())
                        )
                    }
                    recordHistory(reading.vehicle, stored + pending)
                }
                .onFailure {
                    _uiState.update { it.copy(readState = ReadState.Error) }
                }
        }
    }

    /** Run the AI verdict (Pro). Separate, explicit action over the already-read codes. */
    fun runDiagnosis() {
        val state = _uiState.value
        val vehicle = state.vehicle ?: return
        if (state.diagnosis == DiagnosisUiState.Loading) return
        _uiState.update { it.copy(diagnosis = DiagnosisUiState.Loading) }
        viewModelScope.launch {
            val codes = state.storedCodes + state.pendingCodes
            runCatching { diagnosisProvider.diagnose(vehicle, codes) }
                .onSuccess { result ->
                    _uiState.update { it.copy(diagnosis = DiagnosisUiState.Success(result)) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(diagnosis = DiagnosisUiState.Error(DiagnosisErrorType.GENERIC))
                    }
                }
        }
    }

    /** Clear faults in the car (Mode 04) — free, never gated behind Pro. */
    fun clearCodes() {
        if (_uiState.value.isClearing) return
        _uiState.update { it.copy(isClearing = true) }
        viewModelScope.launch {
            val ok = runCatching { vehicleRepository.clearCodes() }.getOrDefault(false)
            if (ok) {
                _history.update { items -> items.map { it.copy(cleared = true) } }
                _uiState.update {
                    it.copy(
                        isClearing = false,
                        storedCodes = emptyList(),
                        pendingCodes = emptyList(),
                        diagnosis = DiagnosisUiState.Locked,
                        inspection = mockInspection(hasCodes = false)
                    )
                }
            } else {
                _uiState.update { it.copy(isClearing = false) }
            }
        }
    }

    private fun recordHistory(vehicle: VehicleContext, codes: List<Dtc>) {
        val now = System.currentTimeMillis()
        val item = HistoryItem(
            id = now,
            title = listOfNotNull(vehicle.make, vehicle.model).joinToString(" ").ifBlank { "Vehicle" },
            subtitle = "${codes.size} code(s)",
            vin = vehicle.vin,
            timestampMillis = now,
            codes = codes
        )
        _history.update { listOf(item) + it }
    }

    private fun mockInspection(hasCodes: Boolean): InspectionStatus =
        if (hasCodes) InspectionStatus.WARN else InspectionStatus.PASS
}
