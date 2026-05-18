package com.storagedoctor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.storagedoctor.ui.screens.DashboardScreen
import com.storagedoctor.ui.screens.CompressionProgressScreen
import com.storagedoctor.ui.screens.ReportsScreen
import com.storagedoctor.ui.screens.SettingsScreen
import com.storagedoctor.ui.screens.StorageAnalyzeScreen

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object StorageAnalyze : Screen("storage_analyze")
    data object CompressionProgress : Screen("compression_progress")
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
}

@Composable
fun StorageDoctorNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAnalyze = { navController.navigate(Screen.StorageAnalyze.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.StorageAnalyze.route) {
            StorageAnalyzeScreen(
                onStartCompression = { navController.navigate(Screen.CompressionProgress.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CompressionProgress.route) {
            CompressionProgressScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
