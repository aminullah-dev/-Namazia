package com.kabulsignal.azanapp.di

import android.content.Context
import androidx.room.Room
import com.kabulsignal.azanapp.data.AzanDatabase
import com.kabulsignal.azanapp.data.PrayerTimesApi
import com.kabulsignal.azanapp.data.PrayerTimesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePrayerTimesApi(retrofit: Retrofit): PrayerTimesApi {
        return retrofit.create(PrayerTimesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AzanDatabase {
        return Room.databaseBuilder(
            context,
            AzanDatabase::class.java,
            "azan_database"
        )
            // This table is a re-fetchable cache of API results, never user-authored data.
            // Dropping it on a schema change is safe and avoids shipping an update that
            // crashes on launch for anyone with the old schema.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(db: AzanDatabase): PrayerTimesDao = db.prayerTimesDao()
}
