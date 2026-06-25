package com.kabulsignal.azanapp.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kabulsignal.azanapp.ui.theme.AzanAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val settings by viewModel.settings.collectAsState()
            val tasbihCount by viewModel.tasbihCount.collectAsState()

            AzanAppTheme(darkTheme = settings.darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AzanNavGraph(
                        uiState = uiState,
                        settings = settings,
                        tasbihCount = tasbihCount,
                        onCitySelected = viewModel::selectCity,
                        onPrayerToggled = viewModel::togglePrayer,
                        onReminderChanged = viewModel::updateReminderMinutes,
                        onCalcMethodChanged = viewModel::updateCalculationMethod,
                        onDarkModeToggled = viewModel::toggleDarkMode,
                        onVibrationToggled = viewModel::toggleVibration,
                        onTasbihIncrement = viewModel::incrementTasbih,
                        onTasbihReset = viewModel::resetTasbih,
                        onRefresh = { viewModel.loadPrayerTimes() }
                    )
                }
            }
        }
    }
}
