package com.kabulsignal.azanapp.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.kabulsignal.azanapp.R

class AzanWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
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
            val views = RemoteViews(context.packageName, R.layout.widget_azan).apply {
                setTextViewText(R.id.widget_city, cityName)
                setTextViewText(R.id.widget_prayer_name, nextPrayerName)
                setTextViewText(R.id.widget_prayer_time, nextPrayerTime)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
