package com.kabulsignal.azanapp.utils

import com.kabulsignal.azanapp.data.AppSettings
import com.kabulsignal.azanapp.data.PrayerName
import com.kabulsignal.azanapp.data.PrayerTimesEntity
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** All prayer times come from the aladhan API in Afghanistan local time, so all
 *  "now"/"today" comparisons must use this zone regardless of the device's timezone. */
val APP_ZONE: ZoneId = ZoneId.of("Asia/Kabul")

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
    now: LocalTime = LocalTime.now(APP_ZONE)
): NextPrayerInfo? = enabledPrayers(settings).firstNotNullOfOrNull { (prayer, timeStr) ->
    val parsed = runCatching { LocalTime.parse(timeStr, hhmm) }.getOrNull()
    if (parsed != null && parsed.isAfter(now)) NextPrayerInfo(prayer, timeStr) else null
}
