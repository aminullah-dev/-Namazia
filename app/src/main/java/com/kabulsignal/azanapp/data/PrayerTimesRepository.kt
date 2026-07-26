package com.kabulsignal.azanapp.data

import android.util.Log
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneOffset
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
        method: Int = 3,
        school: Int = 1
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
                method = method,
                school = school
            )

            val body = response.body()
            when {
                response.isSuccessful && body != null -> {
                    val entity = body.data.toEntity(city.nameEn, date)
                    dao.insert(entity)
                    Result.success(entity)
                }
                response.isSuccessful ->
                    Result.failure(AppException(AppError.BAD_RESPONSE, "empty body"))
                else ->
                    Result.failure(AppException(AppError.SERVER, "HTTP ${response.code()}"))
            }
        } catch (e: IOException) {
            // No connectivity (or it dropped mid-request) and the cache had nothing for this day.
            Log.w("Repository", "Offline and uncached for $dateStr", e)
            Result.failure(AppException(AppError.OFFLINE_NO_CACHE, e.message, e))
        } catch (e: Exception) {
            Log.e("Repository", "Unexpected failure for $dateStr", e)
            Result.failure(AppException(AppError.UNKNOWN, e.message, e))
        }
    }

    suspend fun prefetchWeek(city: AfghanCity, method: Int = 3, school: Int = 1) {
        val today = LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE)
        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            getPrayerTimes(date, city, method, school)
        }
    }

    suspend fun getMonthlyCalendar(
        year: Int,
        month: Int,
        city: AfghanCity,
        method: Int = 3,
        school: Int = 1
    ): Result<List<PrayerData>> {
        return try {
            val response = api.getMonthlyCalendar(
                year = year,
                month = month,
                latitude = city.latitude,
                longitude = city.longitude,
                method = method,
                school = school
            )
            val body = response.body()
            when {
                response.isSuccessful && body != null -> Result.success(body.data)
                response.isSuccessful ->
                    Result.failure(AppException(AppError.BAD_RESPONSE, "empty body"))
                else ->
                    Result.failure(AppException(AppError.SERVER, "HTTP ${response.code()}"))
            }
        } catch (e: IOException) {
            Log.w("Repository", "Monthly calendar offline", e)
            Result.failure(AppException(AppError.OFFLINE_NO_CACHE, e.message, e))
        } catch (e: Exception) {
            Log.e("Repository", "Monthly calendar error", e)
            Result.failure(AppException(AppError.UNKNOWN, e.message, e))
        }
    }

    suspend fun clearOldCache() {
        val threshold = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        dao.deleteOldCache(threshold)
    }

    /** Wipe cached times — call when the calculation method or fiqh school changes so stale
     *  (differently-calculated) times are not served from the cache. */
    suspend fun clearCache() = dao.clearAll()

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
