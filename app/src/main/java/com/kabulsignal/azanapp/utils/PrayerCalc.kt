package com.kabulsignal.azanapp.utils

import com.kabulsignal.azanapp.data.AppSettings
import com.kabulsignal.azanapp.data.PrayerName
import com.kabulsignal.azanapp.data.PrayerTimesEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class NextPrayerInfo(val prayer: PrayerName, val time: String)

private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

/** Enabled prayers of this day paired with their HH:mm time, in chronological order. */
fun PrayerTimesEntity.enabledPrayers(settings: AppSettings): List<Pair<PrayerName, String>> = listOf(
    PrayerName.FAJR to fajr,
    PrayerName.SUNRISE to sunrise,
    PrayerName.DHUHR to dhuhr,
    PrayerName.ASR to asr,
    PrayerName.MAGHRIB to maghrib,
    PrayerName.ISHA to isha
).filter { (p, _) ->
    when (p) {
        PrayerName.FAJR -> settings.fajrEnabled
        PrayerName.SUNRISE -> settings.sunriseEnabled
        PrayerName.DHUHR -> settings.dhuhrEnabled
        PrayerName.ASR -> settings.asrEnabled
        PrayerName.MAGHRIB -> settings.maghribEnabled
        PrayerName.ISHA -> settings.ishaEnabled
    }
}

/** The next enabled prayer strictly after [now], or null if all of today's prayers have passed. */
fun PrayerTimesEntity.upcomingPrayer(
    settings: AppSettings,
    now: LocalTime = LocalTime.now()
): NextPrayerInfo? = enabledPrayers(settings).firstNotNullOfOrNull { (prayer, timeStr) ->
    val parsed = runCatching { LocalTime.parse(timeStr, hhmm) }.getOrNull()
    if (parsed != null && parsed.isAfter(now)) NextPrayerInfo(prayer, timeStr) else null
}
