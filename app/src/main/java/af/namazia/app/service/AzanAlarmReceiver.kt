package af.namazia.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import af.namazia.app.data.AfghanCities
import af.namazia.app.data.PrayerTimesRepository
import af.namazia.app.data.SettingsDataStore
import af.namazia.app.utils.AlarmScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Hilt entry point so plain BroadcastReceivers can obtain singletons without
 * @AndroidEntryPoint (which would require super.onReceive() — not allowed on the
 * abstract BroadcastReceiver.onReceive in Kotlin).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RescheduleEntryPoint {
    fun repository(): PrayerTimesRepository
    fun settingsDataStore(): SettingsDataStore
}

/**
 * Rebuilds the day's alarm schedule off the main thread using goAsync().
 * Shared by the nightly refresh alarm and by boot completion so azan keeps
 * firing without the user having to open the app.
 */
private fun BroadcastReceiver.rescheduleInBackground(context: Context) {
    val pending = goAsync()
    val appContext = context.applicationContext
    val entryPoint = EntryPointAccessors.fromApplication(appContext, RescheduleEntryPoint::class.java)
    val repository = entryPoint.repository()
    val settingsDataStore = entryPoint.settingsDataStore()

    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        try {
            val settings = settingsDataStore.settings.first()
            val city = AfghanCities.list.getOrElse(settings.cityIndex) { AfghanCities.default }
            repository.getPrayerTimes(
                LocalDate.now(af.namazia.app.utils.APP_ZONE),
                city, settings.calculationMethod, settings.asrSchool
            )
                .onSuccess { AlarmScheduler.scheduleTodayAlarms(appContext, it, settings) }
                .onFailure { Log.e("Reschedule", "today fetch failed", it) }
            // Arm the next nightly refresh so this keeps rolling day after day.
            AlarmScheduler.scheduleDailyRefresh(appContext)
            af.namazia.app.ui.widget.AzanWidget.requestUpdate(appContext)
        } catch (e: Exception) {
            Log.e("Reschedule", "failed", e)
        } finally {
            pending.finish()
        }
    }
}

class AzanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AzanAlarmReceiver", "Received: ${intent.action}")

        // Nightly refresh alarm — rebuild the day's schedule.
        if (intent.action == AlarmScheduler.ACTION_DAILY_REFRESH) {
            rescheduleInBackground(context)
            return
        }

        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: return
        val prayerDari = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_DARI) ?: ""
        val prayerTime = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_TIME) ?: ""
        val isReminder = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_REMINDER, false)
        val vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)

        val serviceIntent = Intent(context, AzanService::class.java).apply {
            action = intent.action
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AlarmScheduler.EXTRA_PRAYER_DARI, prayerDari)
            putExtra(AlarmScheduler.EXTRA_PRAYER_TIME, prayerTime)
            putExtra(AlarmScheduler.EXTRA_IS_REMINDER, isReminder)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, vibrate)
        }

        context.startForegroundService(serviceIntent)

        // A prayer just triggered — nudge the widget so it advances to the next one.
        if (!isReminder) {
            af.namazia.app.ui.widget.AzanWidget.requestUpdate(context.applicationContext)
        }
    }
}

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted — rescheduling alarms")
            rescheduleInBackground(context)
        }
    }
}
