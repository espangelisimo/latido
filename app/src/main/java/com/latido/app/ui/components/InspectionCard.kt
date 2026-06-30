package com.latido.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.latido.app.R
import com.latido.app.domain.model.Severity
import com.latido.app.ui.InspectionStatus
import com.latido.app.ui.theme.visual

/**
 * "Will it pass the inspection?" card. The label is localized per country (ITV/MOT/TÜV/…).
 * Phase 1 shows a mock status; the real readiness-monitor logic arrives in Phase 5.
 */
@Composable
fun InspectionCard(status: InspectionStatus, modifier: Modifier = Modifier) {
    val (severity, messageRes) = when (status) {
        InspectionStatus.PASS -> Severity.GREEN to R.string.inspection_pass
        InspectionStatus.WARN -> Severity.AMBER to R.string.inspection_warn
        InspectionStatus.FAIL -> Severity.RED to R.string.inspection_fail
        InspectionStatus.UNKNOWN -> Severity.UNKNOWN to R.string.inspection_warn
    }
    val visual = severity.visual()

    SectionCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.FactCheck,
                contentDescription = null,
                tint = visual.accent,
                modifier = Modifier.size(28.dp)
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    text = stringResource(
                        R.string.inspection_title
                    ) + " (" + stringResource(R.string.inspection_label) + ")",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(messageRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = visual.accent,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
