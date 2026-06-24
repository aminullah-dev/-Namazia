package com.kabulsignal.azanapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kabulsignal.azanapp.utils.AlarmScheduler

class AzanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AzanAlarmReceiver", "Received: ${intent.action}")

        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: return
        val prayerDari = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_DARI) ?: ""
        val prayerTime = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_TIME) ?: ""
        val isReminder = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_REMINDER, false)

        val serviceIntent = Intent(context, AzanService::class.java).apply {
            action = intent.action
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AlarmScheduler.EXTRA_PRAYER_DARI, prayerDari)
            putExtra(AlarmScheduler.EXTRA_PRAYER_TIME, prayerTime)
            putExtra(AlarmScheduler.EXTRA_IS_REMINDER, isReminder)
        }

        context.startForegroundService(serviceIntent)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted — rescheduling alarms")
            val workIntent = Intent(context, AzanService::class.java).apply {
                action = AzanService.ACTION_RESCHEDULE
            }
            context.startForegroundService(workIntent)
        }
    }
}
