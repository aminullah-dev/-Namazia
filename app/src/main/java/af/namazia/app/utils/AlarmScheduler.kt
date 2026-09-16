package af.namazia.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import af.namazia.app.data.AppSettings
import af.namazia.app.data.PrayerName
import af.namazia.app.data.PrayerTimesEntity
import af.namazia.app.service.AzanAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object AlarmScheduler {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val KABUL_ZONE: ZoneId = ZoneId.of("Asia/Kabul")

    fun scheduleTodayAlarms(
        context: Context,
        prayerTimes: PrayerTimesEntity,
        settings: AppSettings
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val today = LocalDate.now(KABUL_ZONE)

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

        // Make sure the nightly re-scheduling alarm is armed whenever we (re)schedule a day.
        scheduleDailyRefresh(context)
    }

    /**
     * Arms a single exact alarm for the next 00:05 (Asia/Kabul). When it fires, AzanService
     * re-fetches the day's prayer times and reschedules — so azan keeps working day after day
     * even if the user never opens the app.
     */
    fun scheduleDailyRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = ZonedDateTime.now(KABUL_ZONE)
        var next = now.toLocalDate().atTime(0, 5).atZone(KABUL_ZONE)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val triggerMs = next.toInstant().toEpochMilli()

        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = ACTION_DAILY_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "daily_refresh".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(alarmManager, triggerMs, pendingIntent)
        Log.d("AlarmScheduler", "Daily refresh armed for $next")
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
                .atZone(KABUL_ZONE)
                .toInstant()
                .toEpochMilli()

            val now = System.currentTimeMillis()
            if (triggerMs <= now) return

            val azanIntent = createAlarmIntent(context, prayer, timeStr, isReminder = false, settings = settings)
            setExactAlarm(alarmManager, triggerMs, azanIntent)

            if (settings.reminderMinutes > 0 && prayer != PrayerName.SUNRISE) {
                val reminderMs = triggerMs - (settings.reminderMinutes * 60 * 1000L)
                if (reminderMs > now) {
                    val reminderIntent = createAlarmIntent(context, prayer, timeStr, isReminder = true, settings = settings)
                    setExactAlarm(alarmManager, reminderMs, reminderIntent)
                }
            }

            Log.d("AlarmScheduler", "Scheduled ${prayer.name} at $timeStr")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule ${prayer.name}", e)
        }
    }

    private fun createAlarmIntent(
        context: Context,
        prayer: PrayerName,
        time: String,
        isReminder: Boolean,
        settings: AppSettings
    ): PendingIntent {
        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = if (isReminder) ACTION_REMINDER else ACTION_AZAN
            putExtra(EXTRA_PRAYER_NAME, prayer.name)
            // Rendered here, at schedule time, in the language chosen then. A later
            // language change reschedules everything, which is what keeps this honest.
            putExtra(
                EXTRA_PRAYER_DARI,
                context.withLanguage(settings.language).getString(prayer.nameRes)
            )
            putExtra(EXTRA_PRAYER_TIME, time)
            putExtra(EXTRA_IS_REMINDER, isReminder)
            putExtra(EXTRA_VIBRATE, settings.vibrationEnabled)
            putExtra(EXTRA_LANGUAGE, settings.language.code)
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
        // Cancellation matches on component + action + request code, not on the intent's
        // extras — so the settings passed here are never actually used and a default is fine.
        val settings = AppSettings()
        PrayerName.entries.forEach { prayer ->
            listOf(false, true).forEach { isReminder ->
                val intent = createAlarmIntent(context, prayer, "", isReminder, settings)
                alarmManager.cancel(intent)
            }
        }
    }

    const val ACTION_AZAN = "af.namazia.app.ACTION_AZAN"
    const val ACTION_REMINDER = "af.namazia.app.ACTION_REMINDER"
    const val ACTION_DAILY_REFRESH = "af.namazia.app.ACTION_DAILY_REFRESH"
    const val EXTRA_PRAYER_NAME = "prayer_name"
    const val EXTRA_PRAYER_DARI = "prayer_dari"
    const val EXTRA_PRAYER_TIME = "prayer_time"
    const val EXTRA_IS_REMINDER = "is_reminder"
    const val EXTRA_VIBRATE = "vibrate"
    /** The language chosen when the alarm was scheduled; the service builds its
     *  notification text in it. A later language change reschedules everything. */
    const val EXTRA_LANGUAGE = "language"
}
