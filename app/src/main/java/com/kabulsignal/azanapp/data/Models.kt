package com.kabulsignal.azanapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class PrayerTimesResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings,
    val date: DateInfo,
    val meta: MetaInfo
)

data class Timings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String,
    val Midnight: String
)

data class DateInfo(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

data class HijriDate(
    val date: String,
    val day: String,
    val month: HijriMonth,
    val year: String
)

data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

data class GregorianDate(
    val date: String,
    val day: String,
    val month: GregorianMonth,
    val year: String,
    val weekday: Weekday
)

data class GregorianMonth(val number: Int, val en: String)
data class Weekday(val en: String)
data class MetaInfo(val latitude: Double, val longitude: Double, val timezone: String)

@Entity(tableName = "prayer_times")
data class PrayerTimesEntity(
    @PrimaryKey
    val date: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDate: String,
    val city: String,
    val cachedAt: Long = System.currentTimeMillis()
)

data class PrayerTime(
    val name: String,
    val nameEn: String,
    val time: String,
    val isNext: Boolean = false,
    val isPast: Boolean = false,
    val enabled: Boolean = true
)

enum class PrayerName(val dari: String, val arabic: String) {
    FAJR("فجر", "Fajr"),
    SUNRISE("طلوع آفتاب", "Sunrise"),
    DHUHR("ظهر", "Dhuhr"),
    ASR("عصر", "Asr"),
    MAGHRIB("مغرب", "Maghrib"),
    ISHA("عشا", "Isha")
}

data class AfghanCity(
    val nameDari: String,
    val nameEn: String,
    val latitude: Double,
    val longitude: Double
)

object AfghanCities {
    val list = listOf(
        AfghanCity("کابل", "Kabul", 34.5553, 69.2075),
        AfghanCity("هرات", "Herat", 34.3529, 62.2040),
        AfghanCity("مزار شریف", "Mazar-i-Sharif", 36.7069, 67.1100),
        AfghanCity("قندهار", "Kandahar", 31.6289, 65.7372),
        AfghanCity("جلال‌آباد", "Jalalabad", 34.4415, 70.4360),
        AfghanCity("کندز", "Kunduz", 36.7285, 68.8571),
        AfghanCity("بامیان", "Bamyan", 34.8203, 67.8294),
        AfghanCity("غزنی", "Ghazni", 33.5450, 68.4231),
        AfghanCity("لشکرگاه", "Lashkar Gah", 31.5933, 64.3599),
        AfghanCity("تالقان", "Taloqan", 36.7364, 69.5391),
        AfghanCity("پل‌خمری", "Pul-e-Khumri", 35.9439, 68.7152),
        AfghanCity("میمنه", "Maimana", 35.9231, 64.7686),
        AfghanCity("شبرغان", "Sheberghan", 36.6700, 65.7500),
        AfghanCity("زرنج", "Zaranj", 30.9587, 61.8686),
        AfghanCity("فیض‌آباد", "Fayzabad", 37.1194, 70.5797)
    )

    val default = list.first()
}

data class AppSettings(
    val cityIndex: Int = 0,
    val calculationMethod: Int = 3,
    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val sunriseEnabled: Boolean = false,
    val reminderMinutes: Int = 15,
    val vibrationEnabled: Boolean = true,
    val useAutoLocation: Boolean = false
)
