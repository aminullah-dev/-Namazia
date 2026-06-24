package com.kabulsignal.azanapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "azan_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CITY_INDEX = intPreferencesKey("city_index")
        val CALC_METHOD = intPreferencesKey("calc_method")
        val FAJR_ENABLED = booleanPreferencesKey("fajr_enabled")
        val DHUHR_ENABLED = booleanPreferencesKey("dhuhr_enabled")
        val ASR_ENABLED = booleanPreferencesKey("asr_enabled")
        val MAGHRIB_ENABLED = booleanPreferencesKey("maghrib_enabled")
        val ISHA_ENABLED = booleanPreferencesKey("isha_enabled")
        val SUNRISE_ENABLED = booleanPreferencesKey("sunrise_enabled")
        val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val USE_AUTO_LOCATION = booleanPreferencesKey("use_auto_location")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppSettings(
                cityIndex = prefs[Keys.CITY_INDEX] ?: 0,
                calculationMethod = prefs[Keys.CALC_METHOD] ?: 3,
                fajrEnabled = prefs[Keys.FAJR_ENABLED] ?: true,
                dhuhrEnabled = prefs[Keys.DHUHR_ENABLED] ?: true,
                asrEnabled = prefs[Keys.ASR_ENABLED] ?: true,
                maghribEnabled = prefs[Keys.MAGHRIB_ENABLED] ?: true,
                ishaEnabled = prefs[Keys.ISHA_ENABLED] ?: true,
                sunriseEnabled = prefs[Keys.SUNRISE_ENABLED] ?: false,
                reminderMinutes = prefs[Keys.REMINDER_MINUTES] ?: 15,
                vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
                useAutoLocation = prefs[Keys.USE_AUTO_LOCATION] ?: false
            )
        }

    suspend fun updateCityIndex(index: Int) = context.dataStore.edit {
        it[Keys.CITY_INDEX] = index
    }

    suspend fun updatePrayerEnabled(prayer: PrayerName, enabled: Boolean) =
        context.dataStore.edit { prefs ->
            when (prayer) {
                PrayerName.FAJR -> prefs[Keys.FAJR_ENABLED] = enabled
                PrayerName.DHUHR -> prefs[Keys.DHUHR_ENABLED] = enabled
                PrayerName.ASR -> prefs[Keys.ASR_ENABLED] = enabled
                PrayerName.MAGHRIB -> prefs[Keys.MAGHRIB_ENABLED] = enabled
                PrayerName.ISHA -> prefs[Keys.ISHA_ENABLED] = enabled
                PrayerName.SUNRISE -> prefs[Keys.SUNRISE_ENABLED] = enabled
            }
        }

    suspend fun updateReminderMinutes(minutes: Int) = context.dataStore.edit {
        it[Keys.REMINDER_MINUTES] = minutes
    }

    suspend fun updateVibration(enabled: Boolean) = context.dataStore.edit {
        it[Keys.VIBRATION_ENABLED] = enabled
    }

    suspend fun updateAutoLocation(enabled: Boolean) = context.dataStore.edit {
        it[Keys.USE_AUTO_LOCATION] = enabled
    }
}
