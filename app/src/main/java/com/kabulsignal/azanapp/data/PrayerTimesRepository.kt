package com.kabulsignal.azanapp.data

import android.util.Log
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerTimesRepository @Inject constructor(
    private val api: PrayerTimesApi,
    private val dao: PrayerTimesDao
) {
    suspend fun getPrayerTimes(
        date: LocalDate,
        city: AfghanCity,
        method: Int = 3
    ): Result<PrayerTimesEntity> {
        val dateStr = date.toString() // yyyy-MM-dd

        val cached = dao.getByDateAndCity(dateStr, city.nameEn)
        if (cached != null) {
            return Result.success(cached)
        }

        return try {
            val timestamp = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC)

            val response = api.getTimingsByCoordinates(
                timestamp = timestamp,
                latitude = city.latitude,
                longitude = city.longitude,
                method = method
            )

            if (response.isSuccessful) {
                val body = response.body()!!
                val entity = body.data.toEntity(city.nameEn, date)
                dao.insert(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("Repository", "Network error", e)
            Result.failure(e)
        }
    }

    suspend fun prefetchWeek(city: AfghanCity, method: Int = 3) {
        val today = LocalDate.now()
        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            getPrayerTimes(date, city, method)
        }
    }

    suspend fun clearOldCache() {
        val threshold = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        dao.deleteOldCache(threshold)
    }

    private fun PrayerData.toEntity(cityName: String, requestDate: LocalDate): PrayerTimesEntity {
        return PrayerTimesEntity(
            date = requestDate.toString(),
            fajr = cleanTime(timings.Fajr),
            sunrise = cleanTime(timings.Sunrise),
            dhuhr = cleanTime(timings.Dhuhr),
            asr = cleanTime(timings.Asr),
            maghrib = cleanTime(timings.Maghrib),
            isha = cleanTime(timings.Isha),
            hijriDate = "${date.hijri.day} ${date.hijri.month.ar} ${date.hijri.year}",
            city = cityName
        )
    }

    // API returns "04:12 (AFT)" — strip timezone suffix
    private fun cleanTime(time: String): String = time.split(" ").first()
}
