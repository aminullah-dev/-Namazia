package com.kabulsignal.azanapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimesDao {

    @Query("SELECT * FROM prayer_times WHERE date = :date AND city = :city LIMIT 1")
    suspend fun getByDateAndCity(date: String, city: String): PrayerTimesEntity?

    @Query("SELECT * FROM prayer_times WHERE city = :city ORDER BY date ASC")
    fun getAllByCity(city: String): Flow<List<PrayerTimesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prayerTimes: PrayerTimesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<PrayerTimesEntity>)

    @Query("DELETE FROM prayer_times WHERE cachedAt < :threshold")
    suspend fun deleteOldCache(threshold: Long)

    @Query("DELETE FROM prayer_times")
    suspend fun clearAll()
}

@Database(entities = [PrayerTimesEntity::class], version = 1, exportSchema = false)
abstract class AzanDatabase : RoomDatabase() {
    abstract fun prayerTimesDao(): PrayerTimesDao
}
