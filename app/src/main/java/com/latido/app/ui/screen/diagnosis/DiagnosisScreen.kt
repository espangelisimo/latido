package com.latido.app.ui.screen.diagnosis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latido.app.R
import com.latido.app.data.obd.ConnectionPhase
import com.latido.app.ui.CarViewModel
import com.latido.app.ui.DiagnosisUiState
import com.latido.app.ui.ReadErrorReason
import com.latido.app.ui.ReadState
import com.latido.app.ui.components.AnswersCard
import com.latido.app.ui.components.CodeListCard
import com.latido.app.ui.components.CostCard
import com.latido.app.ui.components.DiagnosisLoadingSkeleton
import com.latido.app.ui.components.InspectionCard
import com.latido.app.ui.components.MechanicCard
import com.latido.app.ui.components.ProbableFaultsCard
import com.latido.app.ui.components.SectionCard
import com.latido.app.ui.components.SectionTitle
import com.latido.app.ui.components.VehicleHeader
import com.latido.app.ui.components.VerdictBlock
import com.latido.app.ui.components.VerdictLockedCard

@Composable
fun DiagnosisScreen(viewModel: CarViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val phase by viewModel.connectionPhase.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val rs = state.readState) {
            ReadState.Idle -> EmptyState()
            ReadState.Loading -> LoadingState(phase)
            is ReadState.Error -> ErrorState(rs.reason.messageRes(), onRetry = viewModel::analyze)
            ReadState.Loaded -> {
                state.vehicle?.let { VehicleHeader(it) }

                InspectionCard(state.inspection)

                // FREE basic view (codes + colours + clear), separate from the AI verdict.
                CodeListCard(
                    storedCodes = state.storedCodes,
                    pendingCodes = state.pendingCodes,
                    isClearing = state.isClearing,
                    onClearCodes = viewModel::clearCodes
                )

                // AI verdict (Pro) — only when explicitly requested.
                when (val d = state.diagnosis) {
                    DiagnosisUiState.Locked ->
                        VerdictLockedCard(onRunDiagnosis = viewModel::runDiagnosis)

                    DiagnosisUiState.Loading ->
                        DiagnosisLoadingSkeleton()

                    is DiagnosisUiState.Success -> {
                        VerdictBlock(d.result)
                        ProbableFaultsCard(d.result.probableFaults)
                        AnswersCard(d.result.answers)
                        CostCard(d.result.estimatedCost)
                        MechanicCard(text = d.result.mechanicText, onShare = { /* stub: Phase 4 */ })
                    }

                    is DiagnosisUiState.Error ->
                        ErrorState(R.string.error_no_connection, onRetry = viewModel::runDiagnosis)
                }
            }
        }
    }
}

private fun ReadErrorReason.messageRes(): Int = when (this) {
    ReadErrorReason.NO_ADAPTER -> R.string.error_no_adapter
    ReadErrorReason.BLUETOOTH_OFF -> R.string.error_bluetooth_off
    ReadErrorReason.PERMISSION -> R.string.error_permission
    ReadErrorReason.CONNECTION_FAILED -> R.string.error_connection_failed
    ReadErrorReason.TIMEOUT -> R.string.error_timeout
    ReadErrorReason.GENERIC -> R.string.error_generic
}

@Composable
private fun EmptyState() {
    SectionCard(Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.verdict_title))
        Text(
            text = stringResource(R.string.history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun LoadingState(phase: ConnectionPhase) {
    val statusRes = when (phase) {
        ConnectionPhase.SCANNING -> R.string.status_scanning
        ConnectionPhase.CONNECTING -> R.string.status_connecting
        else -> R.string.status_reading
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(statusRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        DiagnosisLoadingSkeleton(Modifier.fillMaxWidth())
    }
}

@Composable
private fun ErrorState(messageRes: Int, onRetry: () -> Unit) {
    SectionCard(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
