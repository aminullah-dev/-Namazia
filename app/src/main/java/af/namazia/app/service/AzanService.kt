package af.namazia.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import af.namazia.app.AzanApplication
import af.namazia.app.R
import af.namazia.app.ui.MainActivity
import af.namazia.app.data.AppLanguage
import af.namazia.app.utils.AlarmScheduler
import af.namazia.app.utils.withLanguage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Strings are resolved through the language the alarm was scheduled in, not the
     * device's. Without this the prayer name would arrive in Pashto inside a Dari
     * sentence on a phone set to English.
     */
    private var strings: android.content.Context = this

    private fun useLanguage(intent: Intent) {
        val code = intent.getStringExtra(AlarmScheduler.EXTRA_LANGUAGE)
        strings = applicationContext.withLanguage(AppLanguage.fromCode(code))
    }

    companion object {
        const val NOTIFICATION_ID_AZAN = 1001
        const val NOTIFICATION_ID_REMINDER = 1002
        const val ACTION_STOP_AZAN = "af.namazia.app.ACTION_STOP_AZAN"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AlarmScheduler.ACTION_AZAN -> {
                useLanguage(intent)
                handleAzan(intent)
            }
            AlarmScheduler.ACTION_REMINDER -> {
                useLanguage(intent)
                handleReminder(intent)
            }
            ACTION_STOP_AZAN -> stopAzan()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun handleAzan(intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: ""
        val prayerDari = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_DARI) ?: strings.getString(R.string.prayer_generic)
        val prayerTime = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_TIME) ?: ""
        val shouldVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)
        val isFajr = prayerName == "FAJR"

        startForeground(NOTIFICATION_ID_AZAN, buildAzanNotification(prayerDari, prayerTime))
        playAzan(isFajr)
        if (shouldVibrate) vibrate(longArrayOf(0, 500, 200, 500, 200, 500))
        handler.postDelayed({ stopAzan() }, 5 * 60 * 1000L)
    }

    private fun handleReminder(intent: Intent) {
        val prayerDari = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_DARI) ?: strings.getString(R.string.prayer_generic)
        val prayerTime = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_TIME) ?: ""
        val shouldVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)

        startForeground(NOTIFICATION_ID_REMINDER, buildReminderNotification(prayerDari, prayerTime))
        if (shouldVibrate) vibrate(longArrayOf(0, 300, 100, 300))
        handler.postDelayed({ stopSelf() }, 3000)
    }

    private fun playAzan(isFajr: Boolean = false) {
        try {
            mediaPlayer?.release()

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // For Fajr: try azan_fajr.mp3 first, then fall back to azan.mp3
            // For others: use azan.mp3
            val rawId = if (isFajr) {
                resources.getIdentifier("azan_fajr", "raw", packageName)
                    .takeIf { it != 0 }
                    ?: resources.getIdentifier("azan", "raw", packageName)
            } else {
                resources.getIdentifier("azan", "raw", packageName)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                if (rawId != 0) {
                    val afd = resources.openRawResourceFd(rawId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } else {
                    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    setDataSource(applicationContext, alarmUri)
                    handler.postDelayed({ stopAzan() }, 30 * 1000L)
                }
                prepare()
                setOnCompletionListener { stopAzan() }
                start()
            }
            Log.d("AzanService", "Azan started (fajr=$isFajr, customFile=${rawId != 0})")
        } catch (e: Exception) {
            Log.e("AzanService", "Failed to play azan", e)
            handler.postDelayed({ stopSelf() }, 3000)
        }
    }

    private fun stopAzan() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AzanService", "Error stopping media", e)
        }
        handler.removeCallbacksAndMessages(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun vibrate(pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun buildAzanNotification(prayerDari: String, time: String): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, AzanService::class.java).apply { action = ACTION_STOP_AZAN },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AzanApplication.CHANNEL_AZAN)
            .setContentTitle(strings.getString(R.string.notif_azan_title, prayerDari))
            .setContentText(strings.getString(R.string.notif_azan_body, time))
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentIntent(openAppIntent)
            .addAction(R.drawable.ic_stop, strings.getString(R.string.notif_azan_stop), stopIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun buildReminderNotification(prayerDari: String, time: String): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AzanApplication.CHANNEL_REMINDER)
            .setContentTitle(strings.getString(R.string.notif_reminder_title, prayerDari))
            .setContentText(strings.getString(R.string.notif_reminder_body, time))
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentIntent(openAppIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}
