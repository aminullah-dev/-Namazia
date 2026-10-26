package af.namazia.app.utils

import af.namazia.app.data.AppSettings
import af.namazia.app.data.PrayerName
import af.namazia.app.data.PrayerTimesEntity
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrayerCalcTest {
    private val day = PrayerTimesEntity(
        date = "2026-10-26",
        fajr = "04:55",
        sunrise = "06:20",
        dhuhr = "11:45",
        asr = "15:10",
        maghrib = "17:05",
        isha = "18:25",
        hijriDate = "",
        city = "Kabul"
    )

    @Test
    fun sunriseIsOffByDefault() {
        val names = day.enabledPrayers(AppSettings()).map { it.first }
        assertEquals(
            listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA),
            names
        )
    }

    @Test
    fun enabledPrayersKeepChronologicalOrderAndTimes() {
        val all = day.enabledPrayers(AppSettings(sunriseEnabled = true))
        assertEquals(
            listOf(
                PrayerName.FAJR to "04:55",
                PrayerName.SUNRISE to "06:20",
                PrayerName.DHUHR to "11:45",
                PrayerName.ASR to "15:10",
                PrayerName.MAGHRIB to "17:05",
                PrayerName.ISHA to "18:25"
            ),
            all
        )
    }

    @Test
    fun disabledPrayerIsSkipped() {
        val names = day.enabledPrayers(AppSettings(dhuhrEnabled = false)).map { it.first }
        assertEquals(false, PrayerName.DHUHR in names)
    }

    @Test
    fun upcomingIsNextEnabledPrayer() {
        val next = day.upcomingPrayer(AppSettings(), LocalTime.of(12, 0))
        assertEquals(NextPrayerInfo(PrayerName.ASR, "15:10"), next)
    }

    @Test
    fun upcomingSkipsDisabledSunrise() {
        val next = day.upcomingPrayer(AppSettings(), LocalTime.of(5, 0))
        assertEquals(PrayerName.DHUHR, next?.prayer)
    }

    @Test
    fun upcomingIsStrictlyAfterNow() {
        val next = day.upcomingPrayer(AppSettings(), LocalTime.of(11, 45))
        assertEquals(PrayerName.ASR, next?.prayer)
    }

    @Test
    fun upcomingIsNullAfterLastPrayer() {
        assertNull(day.upcomingPrayer(AppSettings(), LocalTime.of(23, 0)))
    }

    @Test
    fun upcomingIgnoresUnparsableTimes() {
        val broken = day.copy(dhuhr = "--:--")
        val next = broken.upcomingPrayer(AppSettings(), LocalTime.of(5, 0))
        assertEquals(PrayerName.ASR, next?.prayer)
    }
}
