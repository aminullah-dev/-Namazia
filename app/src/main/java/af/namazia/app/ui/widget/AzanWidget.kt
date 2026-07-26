package af.namazia.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import af.namazia.app.R
import af.namazia.app.data.AfghanCities
import af.namazia.app.data.PrayerTimesRepository
import af.namazia.app.data.SettingsDataStore
import af.namazia.app.ui.MainActivity
import af.namazia.app.utils.NextPrayerInfo
import af.namazia.app.utils.toPersianDigits
import af.namazia.app.utils.upcomingPrayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class AzanWidget : AppWidgetProvider() {

    @Inject lateinit var repository: PrayerTimesRepository
    @Inject lateinit var settingsDataStore: SettingsDataStore

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = settingsDataStore.settings.first()
                val city = AfghanCities.list.getOrElse(settings.cityIndex) { AfghanCities.default }
                val method = settings.calculationMethod
                val school = settings.asrSchool
                val today = LocalDate.now(af.namazia.app.utils.APP_ZONE)

                // Next prayer today; if all have passed, fall back to tomorrow's Fajr.
                val next: NextPrayerInfo? =
                    repository.getPrayerTimes(today, city, method, school).getOrNull()
                        ?.upcomingPrayer(settings)
                        ?: repository.getPrayerTimes(today.plusDays(1), city, method, school).getOrNull()
                            ?.let { NextPrayerInfo(af.namazia.app.data.PrayerName.FAJR, it.fajr) }

                appWidgetIds.forEach { id ->
                    updateWidget(
                        appContext, appWidgetManager, id,
                        nextPrayerName = next?.prayer?.dari ?: "—",
                        nextPrayerTime = next?.time?.toPersianDigits() ?: "--:--",
                        cityName = city.nameDari
                    )
                }
            } catch (e: Exception) {
                appWidgetIds.forEach { id ->
                    updateWidget(appContext, appWidgetManager, id)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            nextPrayerName: String = "—",
            nextPrayerTime: String = "--:--",
            cityName: String = "کابل"
        ) {
            val launchIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val views = RemoteViews(context.packageName, R.layout.widget_azan).apply {
                setTextViewText(R.id.widget_city, cityName)
                setTextViewText(R.id.widget_prayer_name, nextPrayerName)
                setTextViewText(R.id.widget_prayer_time, nextPrayerTime)
                setOnClickPendingIntent(R.id.widget_root, launchIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /** Ask all placed widgets to refresh (e.g. after prayer times load or an azan fires). */
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, AzanWidget::class.java))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, AzanWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
