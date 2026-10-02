package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.service.AlarmService
import com.example.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Re-schedule all enabled alarms after a device reboot, otherwise
        // every alarm is silently lost on restart.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    val dao = AppDatabase.getDatabase(context).alarmDao()
                    val scheduler = AlarmScheduler(context.applicationContext)
                    var count = 0
                    dao.getEnabledAlarms().forEach { alarm ->
                        scheduler.schedule(alarm)
                        count++
                    }
                    Log.d("AlarmReceiver", "Rescheduled $count alarms after boot")
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Failed to reschedule alarms after boot", e)
                }
            }
            return
        }

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        Log.d("AlarmReceiver", "Received alarm trigger for ID: $alarmId")

        if (alarmId != -1) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (e: Exception) {
                // Android 12+ blocks foreground-service starts from the background unless
                // the triggering alarm was an exact alarm. Fall back to a high-priority
                // full-screen notification so the user still gets woken instead of silence.
                Log.e("AlarmReceiver", "Failed to start foreground AlarmService, using notification fallback", e)
                postFallbackAlarmNotification(context, alarmId)
            }
        }
    }

    private fun postFallbackAlarmNotification(context: Context, alarmId: Int) {
        try {
            val channelId = "alarm_quest_fallback"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    channelId,
                    "AlarmQuest Fallback Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Backup alarm alerts when the ringing service cannot start"
                    setBypassDnd(true)
                }
                nm.createNotificationChannel(channel)
            }

            val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("RINGING_ALARM_ID", alarmId)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                alarmId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("ALARM RINGING")
                .setContentText("Tap to open your wake-up challenge")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .build()

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(60000 + alarmId, notification)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Fallback notification also failed", e)
        }
    }
}
