package com.latido.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.latido.app.R
import com.latido.app.domain.model.Level

/** Standard card used across the app: rounded, subtle, generous padding. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

/** A small coloured dot used to flag severity next to a code. */
@Composable
fun SeverityDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun probabilityLabel(level: Level): String = stringResource(
    when (level) {
        Level.HIGH -> R.string.probability_high
        Level.MEDIUM -> R.string.probability_medium
        Level.LOW -> R.string.probability_low
    }
)

@Composable
fun confidenceLabel(level: Level): String = stringResource(
    when (level) {
        Level.HIGH -> R.string.confidence_high
        Level.MEDIUM -> R.string.confidence_medium
        Level.LOW -> R.string.confidence_low
    }
)
