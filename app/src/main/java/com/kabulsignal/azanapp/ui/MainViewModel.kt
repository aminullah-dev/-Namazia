package com.kabulsignal.azanapp.ui

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kabulsignal.azanapp.data.*
import com.kabulsignal.azanapp.service.AzanService
import com.kabulsignal.azanapp.ui.CalendarUiState
import com.kabulsignal.azanapp.utils.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val prayerTimes: List<PrayerTime> = emptyList(),
    val nextPrayer: PrayerTime? = null,
    val currentCity: AfghanCity = AfghanCities.default,
    val hijriDate: String = "",
    /** Failure code, not a sentence — the UI resolves the wording. */
    val error: AppError? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: PrayerTimesRepository,
    private val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val settings: StateFlow<AppSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val tasbihCount: StateFlow<Int> = settingsDataStore.tasbihCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _calendarState = MutableStateFlow(CalendarUiState())
    val calendarState: StateFlow<CalendarUiState> = _calendarState.asStateFlow()

    init {
        loadPrayerTimes()
    }

    fun loadPrayerTimes(date: LocalDate = LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE)) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val settings = settingsDataStore.settings.first()
            // getOrElse, not [] — a stale/invalid saved index would otherwise crash on launch.
            val city = AfghanCities.list.getOrElse(settings.cityIndex) { AfghanCities.default }

            repository.getPrayerTimes(date, city, settings.calculationMethod, settings.asrSchool)
                .fold(
                    onSuccess = { entity ->
                        val prayers = entity.toPrayerTimeList(settings)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                prayerTimes = prayers,
                                nextPrayer = prayers.firstOrNull { p -> p.isNext },
                                currentCity = city,
                                hijriDate = entity.hijriDate
                            )
                        }
                        if (date == LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE)) {
                            AlarmScheduler.scheduleTodayAlarms(context, entity, settings)
                            com.kabulsignal.azanapp.ui.widget.AzanWidget.requestUpdate(context)
                            // Cache the coming week so the nightly refresh (and offline use) has data.
                            viewModelScope.launch { repository.prefetchWeek(city, settings.calculationMethod, settings.asrSchool) }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(isLoading = false, error = error.toAppError())
                        }
                    }
                )
        }
    }

    fun selectCity(index: Int) {
        viewModelScope.launch {
            settingsDataStore.updateCityIndex(index)
            loadPrayerTimes()
        }
    }

    fun togglePrayer(prayer: PrayerName, enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updatePrayerEnabled(prayer, enabled)
        }
    }

    fun updateReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.updateReminderMinutes(minutes)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateDarkMode(enabled)
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateVibration(enabled)
        }
    }

    fun updateCalculationMethod(method: Int) {
        viewModelScope.launch {
            settingsDataStore.updateCalculationMethod(method)
            // Cached times were computed with the old method — drop them and refetch.
            repository.clearCache()
            _calendarState.update { it.copy(days = emptyList()) }
            loadPrayerTimes()
        }
    }

    fun updateAsrSchool(school: Int) {
        viewModelScope.launch {
            settingsDataStore.updateAsrSchool(school)
            repository.clearCache()
            _calendarState.update { it.copy(days = emptyList()) }
            loadPrayerTimes()
        }
    }

    fun loadMonthlyCalendar(
        year: Int = LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE).year,
        month: Int = LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE).monthValue
    ) {
        viewModelScope.launch {
            _calendarState.update { it.copy(isLoading = true, error = null, year = year, month = month) }
            val settings = settingsDataStore.settings.first()
            // getOrElse, not [] — a stale/invalid saved index would otherwise crash on launch.
            val city = AfghanCities.list.getOrElse(settings.cityIndex) { AfghanCities.default }
            repository.getMonthlyCalendar(year, month, city, settings.calculationMethod, settings.asrSchool)
                .fold(
                    onSuccess = { days ->
                        _calendarState.update { it.copy(isLoading = false, days = days) }
                    },
                    onFailure = { error ->
                        _calendarState.update {
                            it.copy(isLoading = false, error = error.toAppError())
                        }
                    }
                )
        }
    }

    /** Plays the azan immediately so the user can verify audio works (no need to wait for a prayer time). */
    fun testAzan(fajr: Boolean = false) {
        val intent = Intent(context, AzanService::class.java).apply {
            action = AlarmScheduler.ACTION_AZAN
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, if (fajr) "FAJR" else "DHUHR")
            putExtra(AlarmScheduler.EXTRA_PRAYER_DARI, "تست اذان")
            putExtra(AlarmScheduler.EXTRA_PRAYER_TIME, "")
            putExtra(AlarmScheduler.EXTRA_VIBRATE, settings.value.vibrationEnabled)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun incrementTasbih() {
        viewModelScope.launch {
            settingsDataStore.updateTasbihCount(tasbihCount.value + 1)
        }
    }

    fun resetTasbih() {
        viewModelScope.launch {
            settingsDataStore.updateTasbihCount(0)
        }
    }

    private fun PrayerTimesEntity.toPrayerTimeList(settings: AppSettings): List<PrayerTime> {
        val now = LocalTime.now(com.kabulsignal.azanapp.utils.APP_ZONE)

        val raw = listOf(
            Triple(PrayerName.FAJR, fajr, settings.fajrEnabled),
            Triple(PrayerName.SUNRISE, sunrise, settings.sunriseEnabled),
            Triple(PrayerName.DHUHR, dhuhr, settings.dhuhrEnabled),
            Triple(PrayerName.ASR, asr, settings.asrEnabled),
            Triple(PrayerName.MAGHRIB, maghrib, settings.maghribEnabled),
            Triple(PrayerName.ISHA, isha, settings.ishaEnabled)
        )

        var nextFound = false
        return raw.map { (prayer, timeStr, enabled) ->
            val time = LocalTime.parse(timeStr, timeFormatter)
            val isPast = time.isBefore(now)
            val isNext = !isPast && !nextFound && enabled
            if (isNext) nextFound = true

            PrayerTime(
                name = prayer.dari,
                nameEn = prayer.arabic,
                time = timeStr,
                isNext = isNext,
                isPast = isPast,
                enabled = enabled
            )
        }
    }
}
