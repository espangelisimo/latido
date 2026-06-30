package com.latido.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.latido.app.R
import com.latido.app.domain.model.CanDrive
import com.latido.app.domain.model.DiagnosisResult
import com.latido.app.domain.model.GeneralState
import com.latido.app.ui.theme.visual

/** The single dominant block on the diagnosis screen: icon + short phrase + summary. */
@Composable
fun VerdictBlock(result: DiagnosisResult, modifier: Modifier = Modifier) {
    val visual = result.generalState.visual()
    val icon: ImageVector = when (result.generalState) {
        GeneralState.OK -> Icons.Filled.CheckCircle
        GeneralState.CAUTION -> Icons.Filled.Warning
        GeneralState.CRITICAL -> Icons.Filled.Error
    }
    val phrase = stringResource(
        when (result.canDrive) {
            CanDrive.YES -> R.string.verdict_can_drive_yes
            CanDrive.WITH_CAUTION -> R.string.verdict_can_drive_caution
            CanDrive.NO -> R.string.verdict_can_drive_no
        }
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = visual.container)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = visual.accent,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = phrase,
                    style = MaterialTheme.typography.headlineMedium,
                    color = visual.onContainer,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = visual.onContainer,
                modifier = Modifier.padding(top = 14.dp)
            )
            result.safetyWarning?.let { warning ->
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = visual.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.labelLarge,
                        color = visual.onContainer
                    )
                }
            }
            Text(
                text = stringResource(R.string.verdict_confidence, confidenceLabel(result.confidence)),
                style = MaterialTheme.typography.labelLarge,
                color = visual.onContainer,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

/** Shown before the user runs the AI: explains the value and offers the action. */
@Composable
fun VerdictLockedCard(
    onRunDiagnosis: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.verdict_locked_title))
        Text(
            text = stringResource(R.string.verdict_locked_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
        Button(
            onClick = onRunDiagnosis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        ) {
            Text(stringResource(R.string.verdict_locked_cta))
        }
    }
}
