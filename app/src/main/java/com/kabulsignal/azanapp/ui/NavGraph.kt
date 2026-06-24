package com.kabulsignal.azanapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kabulsignal.azanapp.data.AfghanCities
import com.kabulsignal.azanapp.data.AppSettings
import com.kabulsignal.azanapp.data.PrayerName

@Composable
fun AzanNavGraph(
    uiState: HomeUiState,
    settings: AppSettings,
    onCitySelected: (Int) -> Unit,
    onPrayerToggled: (PrayerName, Boolean) -> Unit,
    onReminderChanged: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                uiState = uiState,
                onSettingsClick = { navController.navigate("settings") },
                onRefresh = onRefresh
            )
        }
        composable("settings") {
            SettingsScreen(
                settings = settings,
                cities = AfghanCities.list,
                onCitySelected = onCitySelected,
                onPrayerToggled = onPrayerToggled,
                onReminderChanged = onReminderChanged,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
