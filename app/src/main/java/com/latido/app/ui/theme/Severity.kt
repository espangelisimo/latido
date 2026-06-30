package com.latido.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.latido.app.domain.model.CanDrive
import com.latido.app.domain.model.GeneralState
import com.latido.app.domain.model.Severity

/**
 * The ONLY place green/amber/red live. These colours carry meaning (severity) and must never
 * be used decoratively, or they lose it.
 */
data class SeverityVisual(
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

private val GreenLight = SeverityVisual(Color(0xFF1E7D34), Color(0xFFDCF3E1), Color(0xFF0B3D18))
private val AmberLight = SeverityVisual(Color(0xFFB7791F), Color(0xFFFBEFD2), Color(0xFF4A3208))
private val RedLight = SeverityVisual(Color(0xFFC62828), Color(0xFFFADBDB), Color(0xFF5A0F0F))
private val NeutralLight = SeverityVisual(Color(0xFF5B6470), Color(0xFFE7EAEF), Color(0xFF2A2F37))

private val GreenDark = SeverityVisual(Color(0xFF4ADE80), Color(0xFF143524), Color(0xFFCFF3DA))
private val AmberDark = SeverityVisual(Color(0xFFF1C04A), Color(0xFF3A2E12), Color(0xFFF7E6BC))
private val RedDark = SeverityVisual(Color(0xFFFF6B6B), Color(0xFF3E1717), Color(0xFFF8D2D2))
private val NeutralDark = SeverityVisual(Color(0xFF9AA3AF), Color(0xFF222831), Color(0xFFD7DCE3))

@Composable
@ReadOnlyComposable
fun Severity.visual(): SeverityVisual {
    val dark = isSystemInDarkTheme()
    return when (this) {
        Severity.GREEN -> if (dark) GreenDark else GreenLight
        Severity.AMBER -> if (dark) AmberDark else AmberLight
        Severity.RED -> if (dark) RedDark else RedLight
        Severity.UNKNOWN -> if (dark) NeutralDark else NeutralLight
    }
}

@Composable
@ReadOnlyComposable
fun GeneralState.visual(): SeverityVisual {
    val dark = isSystemInDarkTheme()
    return when (this) {
        GeneralState.OK -> if (dark) GreenDark else GreenLight
        GeneralState.CAUTION -> if (dark) AmberDark else AmberLight
        GeneralState.CRITICAL -> if (dark) RedDark else RedLight
    }
}

/** Map the "can I drive?" answer to the same severity language. */
fun CanDrive.asSeverity(): Severity = when (this) {
    CanDrive.YES -> Severity.GREEN
    CanDrive.WITH_CAUTION -> Severity.AMBER
    CanDrive.NO -> Severity.RED
}
