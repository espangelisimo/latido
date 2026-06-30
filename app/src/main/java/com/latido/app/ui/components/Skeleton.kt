package com.latido.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.latido.app.R

/**
 * Friendly loading state for the AI verdict: a calm skeleton plus reassuring microcopy,
 * never a dry spinner with technical text.
 */
@Composable
fun DiagnosisLoadingSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    SectionCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.verdict_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(Modifier.padding(top = 16.dp)) {
            ShimmerBar(0.6f, alpha)
            ShimmerBar(0.95f, alpha, topPadding = 14.dp)
            ShimmerBar(0.8f, alpha, topPadding = 10.dp)
        }
    }
}

@Composable
private fun ShimmerBar(widthFraction: Float, alpha: Float, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    Box(
        modifier = Modifier
            .padding(top = topPadding)
            .fillMaxWidth(widthFraction)
            .height(18.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.25f))
    )
}
