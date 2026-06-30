package com.latido.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.latido.app.R
import com.latido.app.domain.model.Dtc
import com.latido.app.domain.model.DtcStatus
import com.latido.app.ui.theme.visual

/**
 * The free basic view: each code shows its short name + severity colour, plus a "Clear faults"
 * button. Built as a surface separate from the AI verdict.
 */
@Composable
fun CodeListCard(
    storedCodes: List<Dtc>,
    pendingCodes: List<Dtc>,
    isClearing: Boolean,
    onClearCodes: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        SectionTitle(stringResource(R.string.codes_title))
        val all = storedCodes + pendingCodes
        if (all.isEmpty()) {
            Text(
                text = stringResource(R.string.codes_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        } else {
            all.forEach { dtc ->
                CodeRow(dtc, Modifier.padding(top = 14.dp))
            }
            OutlinedButton(
                onClick = onClearCodes,
                enabled = !isClearing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null)
                Text(
                    text = stringResource(R.string.home_clear_codes_button),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CodeRow(dtc: Dtc, modifier: Modifier = Modifier) {
    val visual = dtc.severity.visual()
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SeverityDot(color = visual.accent)
            Column(
                Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = dtc.code,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dtc.name ?: stringResource(R.string.codes_unknown_name),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(dtc.status)
        }
        if (dtc.safetyCritical) {
            Row(
                modifier = Modifier.padding(start = 26.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = stringResource(R.string.severity_red),
                    tint = visual.accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.codes_safety_critical),
                    style = MaterialTheme.typography.labelLarge,
                    color = visual.accent
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: DtcStatus) {
    val label = stringResource(
        if (status == DtcStatus.STORED) R.string.codes_stored_label else R.string.codes_pending_label
    )
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
