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

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val azanChannel = NotificationChannel(
                CHANNEL_AZAN,
                getString(R.string.channel_azan_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_azan_description)
                enableVibration(true)
                // Audio is played by AzanService so the azan can be stopped; a channel
                // sound would play over it and cannot be interrupted.
                setSound(null, null)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                getString(R.string.channel_reminder_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_reminder_description)
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
