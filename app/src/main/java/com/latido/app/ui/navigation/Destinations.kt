package com.latido.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.latido.app.R

enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home),
    DIAGNOSIS("diagnosis", R.string.nav_diagnosis, Icons.Filled.DirectionsCar),
    HISTORY("history", R.string.nav_history, Icons.AutoMirrored.Filled.List)
}
