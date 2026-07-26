package af.namazia.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import af.namazia.app.data.AfghanCities
import af.namazia.app.data.AppSettings
import af.namazia.app.data.PrayerName

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem("home", "نماز", Icons.Filled.Home),
    NavItem("calendar", "تقویم", Icons.Filled.DateRange),
    NavItem("qibla", "قبله", Icons.Filled.Explore),
    NavItem("dhikr", "ذکر", Icons.Filled.MenuBook),
    NavItem("settings", "تنظیمات", Icons.Filled.Settings)
)

@Composable
fun AzanNavGraph(
    uiState: HomeUiState,
    settings: AppSettings,
    tasbihCount: Int,
    calendarState: CalendarUiState,
    onCitySelected: (Int) -> Unit,
    onPrayerToggled: (PrayerName, Boolean) -> Unit,
    onReminderChanged: (Int) -> Unit,
    onCalcMethodChanged: (Int) -> Unit,
    onAsrSchoolChanged: (Int) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit,
    onVibrationToggled: (Boolean) -> Unit,
    onTasbihIncrement: () -> Unit,
    onTasbihReset: () -> Unit,
    onMonthChanged: (Int, Int) -> Unit,
    onTestAzan: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(uiState = uiState, onRefresh = onRefresh)
            }
            composable("calendar") {
                // Auto-load current month when first navigating here
                LaunchedEffect(Unit) {
                    if (calendarState.days.isEmpty() && !calendarState.isLoading) {
                        onMonthChanged(calendarState.year, calendarState.month)
                    }
                }
                CalendarScreen(
                    calendarState = calendarState,
                    onMonthChanged = onMonthChanged
                )
            }
            composable("qibla") {
                QiblaScreen(city = uiState.currentCity)
            }
            composable("dhikr") {
                DuaScreen(
                    tasbihCount = tasbihCount,
                    onTasbihIncrement = onTasbihIncrement,
                    onTasbihReset = onTasbihReset
                )
            }
            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    cities = AfghanCities.list,
                    onCitySelected = onCitySelected,
                    onPrayerToggled = onPrayerToggled,
                    onReminderChanged = onReminderChanged,
                    onCalcMethodChanged = onCalcMethodChanged,
                    onAsrSchoolChanged = onAsrSchoolChanged,
                    onDarkModeToggled = onDarkModeToggled,
                    onVibrationToggled = onVibrationToggled,
                    onTestAzan = onTestAzan
                )
            }
        }
    }
}
