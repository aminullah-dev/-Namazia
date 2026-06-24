package com.kabulsignal.azanapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.kabulsignal.azanapp.data.AppSettings
import com.kabulsignal.azanapp.data.PrayerName
import com.kabulsignal.azanapp.data.PrayerTimesEntity
import com.kabulsignal.azanapp.service.AzanAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AlarmScheduler {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun scheduleTodayAlarms(
        context: Context,
        prayerTimes: PrayerTimesEntity,
        settings: AppSettings
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val today = LocalDate.now()

        val prayers = mapOf(
            PrayerName.FAJR to Pair(prayerTimes.fajr, settings.fajrEnabled),
            PrayerName.SUNRISE to Pair(prayerTimes.sunrise, settings.sunriseEnabled),
            PrayerName.DHUHR to Pair(prayerTimes.dhuhr, settings.dhuhrEnabled),
            PrayerName.ASR to Pair(prayerTimes.asr, settings.asrEnabled),
            PrayerName.MAGHRIB to Pair(prayerTimes.maghrib, settings.maghribEnabled),
            PrayerName.ISHA to Pair(prayerTimes.isha, settings.ishaEnabled)
        )

        prayers.forEach { (prayer, pair) ->
            val (timeStr, enabled) = pair
            if (enabled) {
                scheduleAlarm(context, alarmManager, today, prayer, timeStr, settings)
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        date: LocalDate,
        prayer: PrayerName,
        timeStr: String,
        settings: AppSettings
    ) {
        try {
            val time = LocalTime.parse(timeStr, timeFormatter)
            val dateTime = LocalDateTime.of(date, time)
            val triggerMs = dateTime
                .atZone(ZoneId.of("Asia/Kabul"))
                .toInstant()
                .toEpochMilli()

            val now = System.currentTimeMillis()
            if (triggerMs <= now) return

            val azanIntent = createAlarmIntent(context, prayer, timeStr, isReminder = false)
            setExactAlarm(alarmManager, triggerMs, azanIntent)

            if (settings.reminderMinutes > 0 && prayer != PrayerName.SUNRISE) {
                val reminderMs = triggerMs - (settings.reminderMinutes * 60 * 1000L)
                if (reminderMs > now) {
                    val reminderIntent = createAlarmIntent(context, prayer, timeStr, isReminder = true)
                    setExactAlarm(alarmManager, reminderMs, reminderIntent)
                }
            }

            Log.d("AlarmScheduler", "Scheduled ${prayer.dari} at $timeStr")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule ${prayer.dari}", e)
        }
    }

    private fun createAlarmIntent(
        context: Context,
        prayer: PrayerName,
        time: String,
        isReminder: Boolean
    ): PendingIntent {
        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = if (isReminder) ACTION_REMINDER else ACTION_AZAN
            putExtra(EXTRA_PRAYER_NAME, prayer.name)
            putExtra(EXTRA_PRAYER_DARI, prayer.dari)
            putExtra(EXTRA_PRAYER_TIME, time)
            putExtra(EXTRA_IS_REMINDER, isReminder)
        }

        val requestCode = "${prayer.name}_${if (isReminder) "reminder" else "azan"}".hashCode()

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setExactAlarm(
        alarmManager: AlarmManager,
        triggerMs: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        PrayerName.entries.forEach { prayer ->
            listOf(false, true).forEach { isReminder ->
                val intent = createAlarmIntent(context, prayer, "", isReminder)
                alarmManager.cancel(intent)
            }
        }
    }

    const val ACTION_AZAN = "com.kabulsignal.azanapp.ACTION_AZAN"
    const val ACTION_REMINDER = "com.kabulsignal.azanapp.ACTION_REMINDER"
    const val EXTRA_PRAYER_NAME = "prayer_name"
    const val EXTRA_PRAYER_DARI = "prayer_dari"
    const val EXTRA_PRAYER_TIME = "prayer_time"
    const val EXTRA_IS_REMINDER = "is_reminder"
}
