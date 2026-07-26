package com.kabulsignal.azanapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabulsignal.azanapp.ui.theme.AzanAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    // The ViewModel loads on init, so skip the resume that immediately follows onCreate.
    private var firstResume = true

    override fun onResume() {
        super.onResume()
        if (firstResume) {
            firstResume = false
        } else {
            // Returning to the app: refresh so the next-prayer card / countdown stay current.
            viewModel.loadPrayerTimes()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only ask when we do not already hold it — launching the request on every start
        // re-prompts users who granted it and wastes the one prompt of users who denied it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val tasbihCount by viewModel.tasbihCount.collectAsStateWithLifecycle()
            val calendarState by viewModel.calendarState.collectAsStateWithLifecycle()

            AzanAppTheme(darkTheme = settings.darkMode) {
                // The whole UI is Dari — force right-to-left regardless of device locale.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AzanNavGraph(
                        uiState = uiState,
                        settings = settings,
                        tasbihCount = tasbihCount,
                        calendarState = calendarState,
                        onCitySelected = viewModel::selectCity,
                        onPrayerToggled = viewModel::togglePrayer,
                        onReminderChanged = viewModel::updateReminderMinutes,
                        onCalcMethodChanged = viewModel::updateCalculationMethod,
                        onAsrSchoolChanged = viewModel::updateAsrSchool,
                        onDarkModeToggled = viewModel::toggleDarkMode,
                        onVibrationToggled = viewModel::toggleVibration,
                        onTasbihIncrement = viewModel::incrementTasbih,
                        onTasbihReset = viewModel::resetTasbih,
                        onMonthChanged = viewModel::loadMonthlyCalendar,
                        onTestAzan = viewModel::testAzan,
                        onRefresh = { viewModel.loadPrayerTimes() }
                    )
                }
                }
            }
        }
    }
}
