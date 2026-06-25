package com.kabulsignal.azanapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kabulsignal.azanapp.data.*
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
    val error: String? = null
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

    init {
        loadPrayerTimes()
    }

    fun loadPrayerTimes(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val settings = settingsDataStore.settings.first()
            val city = AfghanCities.list[settings.cityIndex]

            repository.getPrayerTimes(date, city, settings.calculationMethod)
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
                        if (date == LocalDate.now()) {
                            AlarmScheduler.scheduleTodayAlarms(context, entity, settings)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "خطا در بارگزاری اوقات: ${error.message}"
                            )
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
            loadPrayerTimes()
        }
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
        val now = LocalTime.now()

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
