package com.latido.app.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latido.app.R
import com.latido.app.domain.model.Severity
import com.latido.app.ui.CarViewModel
import com.latido.app.ui.ReadState
import com.latido.app.ui.components.SectionCard
import com.latido.app.ui.components.SectionTitle
import com.latido.app.ui.theme.visual

@Composable
fun HomeScreen(
    viewModel: CarViewModel,
    onAnalyzed: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = state.readState == ReadState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        ConnectionRow(isDemo = state.isDemo, connected = state.isConnected)

        Button(
            onClick = {
                viewModel.analyze()
                onAnalyzed()
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(Icons.Filled.Search, contentDescription = null)
                Text(
                    text = stringResource(R.string.home_analyze_button),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }

        if (state.readState == ReadState.Loaded) {
            HealthSummaryCard(
                hasCodes = state.hasCodes,
                codeCount = state.storedCodes.size + state.pendingCodes.size
            )
        }
    }
}

@Composable
private fun ConnectionRow(isDemo: Boolean, connected: Boolean) {
    val text = when {
        isDemo -> stringResource(R.string.home_connection_demo)
        connected -> stringResource(R.string.home_connection_connected)
        else -> stringResource(R.string.home_connection_disconnected)
    }
    val icon = if (connected || isDemo) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun HealthSummaryCard(hasCodes: Boolean, codeCount: Int) {
    val severity = if (hasCodes) Severity.AMBER else Severity.GREEN
    val visual = severity.visual()
    SectionCard {
        SectionTitle(stringResource(R.string.home_health_summary_title))
        Row(
            modifier = Modifier.padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = visual.accent,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Text(
                text = if (hasCodes) {
                    stringResource(R.string.home_health_faults_found, codeCount)
                } else {
                    stringResource(R.string.home_health_all_good)
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
