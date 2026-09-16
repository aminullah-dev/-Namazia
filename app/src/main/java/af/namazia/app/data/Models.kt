package af.namazia.app.data

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import af.namazia.app.R

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
    /** Resolved at the UI edge so the row follows the chosen language. */
    @StringRes val nameRes: Int,
    val nameEn: String,
    val time: String,
    val isNext: Boolean = false,
    val isPast: Boolean = false,
    val enabled: Boolean = true
)

/**
 * Prayer names carry a resource id, not a spelling: Pashto has its own names for these
 * — ماسپښين for Dhuhr, مازديګر for Asr — so they are translated, not transliterated.
 */
enum class PrayerName(@StringRes val nameRes: Int, val arabic: String) {
    FAJR(R.string.prayer_fajr, "Fajr"),
    SUNRISE(R.string.prayer_sunrise, "Sunrise"),
    DHUHR(R.string.prayer_dhuhr, "Dhuhr"),
    ASR(R.string.prayer_asr, "Asr"),
    MAGHRIB(R.string.prayer_maghrib, "Maghrib"),
    ISHA(R.string.prayer_isha, "Isha")
}

/**
 * [nameEn] is the city's stable identity — the cache key and the API request both hang
 * off it, so it is never translated. [nameRes] is what the user sees, and a few of them
 * genuinely differ between the languages: لشکرگاه is لښکرګاه in Pashto.
 */
data class AfghanCity(
    @StringRes val nameRes: Int,
    val nameEn: String,
    val latitude: Double,
    val longitude: Double
)

object AfghanCities {
    val list = listOf(
        AfghanCity(R.string.city_kabul, "Kabul", 34.5553, 69.2075),
        AfghanCity(R.string.city_herat, "Herat", 34.3529, 62.204),
        AfghanCity(R.string.city_mazar, "Mazar-i-Sharif", 36.7069, 67.11),
        AfghanCity(R.string.city_kandahar, "Kandahar", 31.6289, 65.7372),
        AfghanCity(R.string.city_jalalabad, "Jalalabad", 34.4415, 70.436),
        AfghanCity(R.string.city_kunduz, "Kunduz", 36.7285, 68.8571),
        AfghanCity(R.string.city_bamyan, "Bamyan", 34.8203, 67.8294),
        AfghanCity(R.string.city_ghazni, "Ghazni", 33.545, 68.4231),
        AfghanCity(R.string.city_lashkargah, "Lashkar Gah", 31.5933, 64.3599),
        AfghanCity(R.string.city_taloqan, "Taloqan", 36.7364, 69.5391),
        AfghanCity(R.string.city_pulekhumri, "Pul-e-Khumri", 35.9439, 68.7152),
        AfghanCity(R.string.city_maimana, "Maimana", 35.9231, 64.7686),
        AfghanCity(R.string.city_sheberghan, "Sheberghan", 36.67, 65.75),
        AfghanCity(R.string.city_zaranj, "Zaranj", 30.9587, 61.8686),
        AfghanCity(R.string.city_fayzabad, "Fayzabad", 37.1194, 70.5797)
    )

    val default = list.first()
}

data class AppSettings(
    val cityIndex: Int = 0,
    val calculationMethod: Int = 3,
    // Asr calculation: 0 = Shafi/standard, 1 = Hanafi (default — most Afghans are Hanafi)
    val asrSchool: Int = 1,
    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val sunriseEnabled: Boolean = false,
    val reminderMinutes: Int = 15,
    val vibrationEnabled: Boolean = true,
    val useAutoLocation: Boolean = false,
    val darkMode: Boolean = false,
    /**
     * Dari by default — the wider second language in the cities this app covers. Not a
     * judgement about the languages, just about who opens the app first and has no idea
     * there is a switch.
     */
    val language: AppLanguage = AppLanguage.DARI
)

/** The two languages the app ships in. Both are Afghan national languages and both are
 *  right-to-left, so switching changes wording only — never layout. */
enum class AppLanguage(val code: String, @StringRes val nameRes: Int) {
    DARI("fa", R.string.language_dari),
    PASHTO("ps", R.string.language_pashto);

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: DARI
    }
}

// Fiqh school for Asr timing
data class Madhab(val school: Int, @StringRes val nameRes: Int)

object Madhabs {
    val list = listOf(
        Madhab(1, R.string.madhab_hanafi),
        Madhab(0, R.string.madhab_other)
    )

    @StringRes
    fun nameResOf(school: Int): Int =
        list.firstOrNull { it.school == school }?.nameRes ?: R.string.madhab_hanafi
}

// Calculation methods supported by the aladhan API
data class CalcMethod(val id: Int, @StringRes val nameRes: Int)

object CalcMethods {
    val list = listOf(
        CalcMethod(1, R.string.method_karachi),
        CalcMethod(3, R.string.method_mwl),
        CalcMethod(4, R.string.method_makkah),
        CalcMethod(2, R.string.method_isna),
        CalcMethod(5, R.string.method_egypt)
    )

    @StringRes
    fun nameResOf(id: Int): Int =
        list.firstOrNull { it.id == id }?.nameRes ?: R.string.method_mwl
}
