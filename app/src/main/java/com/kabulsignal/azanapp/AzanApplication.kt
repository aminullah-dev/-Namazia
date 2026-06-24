package com.kabulsignal.azanapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AzanApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val azanChannel = NotificationChannel(
                CHANNEL_AZAN,
                "اذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان وقت نماز با صدای اذان"
                enableVibration(true)
                setSound(null, null)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "یادآوری نماز",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "یادآوری قبل از وقت نماز"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(azanChannel)
            manager.createNotificationChannel(reminderChannel)
        }
    }

    companion object {
        const val CHANNEL_AZAN = "channel_azan"
        const val CHANNEL_REMINDER = "channel_reminder"
    }
}
