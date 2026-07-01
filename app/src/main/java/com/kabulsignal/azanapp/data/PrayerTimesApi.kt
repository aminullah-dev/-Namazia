package com.kabulsignal.azanapp.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerTimesApi {

    @GET("v1/timings/{timestamp}")
    suspend fun getTimingsByCoordinates(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 3,
        @Query("school") school: Int = 1
    ): Response<PrayerTimesResponse>

    @GET("v1/calendar/{year}/{month}")
    suspend fun getMonthlyCalendar(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 3,
        @Query("school") school: Int = 1
    ): Response<MonthlyCalendarResponse>
}

data class MonthlyCalendarResponse(
    val code: Int,
    val status: String,
    val data: List<PrayerData>
)
