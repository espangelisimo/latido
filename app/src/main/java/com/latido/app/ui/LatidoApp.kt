package com.latido.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.latido.app.ui.navigation.Destination
import com.latido.app.ui.screen.diagnosis.DiagnosisScreen
import com.latido.app.ui.screen.history.HistoryScreen
import com.latido.app.ui.screen.home.HomeScreen

@Composable
fun LatidoApp() {
    val navController = rememberNavController()
    // One shared ViewModel (Activity-scoped) so Home and Diagnosis see the same car state.
    val carViewModel: CarViewModel = hiltViewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Single, consistent way to switch top-level tabs. Used by the bottom bar AND by the
    // "Analyze my car" button, so the selected state and back stack never get out of sync.
    fun selectTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { dest ->
                    val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectTab(dest.route) },
                        icon = {
                            Icon(dest.icon, contentDescription = stringResource(dest.labelRes))
                        },
                        label = { Text(stringResource(dest.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.HOME.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Destination.HOME.route) {
                HomeScreen(
                    viewModel = carViewModel,
                    onAnalyzed = { selectTab(Destination.DIAGNOSIS.route) }
                )
            }
            composable(Destination.DIAGNOSIS.route) {
                DiagnosisScreen(viewModel = carViewModel)
            }
            composable(Destination.HISTORY.route) {
                HistoryScreen(viewModel = carViewModel)
            }
        }
    }
}
