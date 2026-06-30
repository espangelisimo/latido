package com.latido.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.latido.app.R
import com.latido.app.domain.model.Answers
import com.latido.app.domain.model.EstimatedCost
import com.latido.app.domain.model.ProbableFault

@Composable
fun ProbableFaultsCard(faults: List<ProbableFault>, modifier: Modifier = Modifier) {
    if (faults.isEmpty()) return
    SectionCard(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.faults_title))
        faults.forEach { fault ->
            Column(Modifier.padding(top = 16.dp)) {
                Text(
                    text = fault.cause,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.probability_label, probabilityLabel(fault.probability)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = fault.explanation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AnswersCard(answers: Answers, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.answers_title))
        QaRow(stringResource(R.string.answer_can_drive), answers.canKeepDriving)
        QaRow(stringResource(R.string.answer_fuel), answers.moreFuel)
        QaRow(stringResource(R.string.answer_damage), answers.canDamageOtherParts)
    }
}

@Composable
private fun QaRow(question: String, answer: String) {
    Column(Modifier.padding(top = 14.dp)) {
        Text(
            text = question,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun CostCard(cost: EstimatedCost, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.cost_title))
        Text(
            text = stringResource(R.string.cost_range, cost.min, cost.max),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = cost.note ?: stringResource(R.string.cost_note_default),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Pro card with the mechanic-ready text and a (stubbed) Share action. */
@Composable
fun MechanicCard(
    text: String,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.mechanic_title))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Text(
                text = stringResource(R.string.mechanic_share),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
