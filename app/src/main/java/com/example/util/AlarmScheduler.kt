package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.Alarm
import com.example.receiver.AlarmReceiver
import java.util.*

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun schedule(alarm: Alarm) {
        if (alarmManager == null || !alarm.enabled) {
            cancel(alarm)
            return
        }

        val triggerTimeMs = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.getRepeatDaysList())
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
        }

        // Distinct requestCode per alarm
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} at $triggerTimeMs (Time: ${Date(triggerTimeMs)})")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Failed to schedule exact alarm due to permission constraints", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        }
    }

    fun cancel(alarm: Alarm) {
        if (alarmManager == null) return

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        Log.d("AlarmScheduler", "Canceled alarm ${alarm.id}")
    }

    companion object {
        fun calculateNextTriggerTime(hour: Int, minute: Int, repeatDays: List<Int>): Long {
            val now = Calendar.getInstance()
            val alarmTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If it's a one-time alarm
            if (repeatDays.isEmpty()) {
                if (alarmTime.before(now)) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                }
                return alarmTime.timeInMillis
            }

            // For repeating alarms, find the closest upcoming day in the repeat list.
            // Note Calendar days are 1 (Sunday) to 7 (Saturday).
            // Our repeat list uses: 1 = Mon, 2 = Tue, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat, 7 = Sun.
            // We need to map Calendar.DAY_OF_WEEK to our 1-based ISO day representation.
            val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
            val isoCurrentDay = when (currentDayOfWeek) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            var daysToAdding = -1
            // Check repeat list starting from today
            for (i in 0..7) {
                val candidateDay = ((isoCurrentDay - 1 + i) % 7) + 1
                if (repeatDays.contains(candidateDay)) {
                    if (i == 0) {
                        // It's today. Check if the time has already passed
                        if (alarmTime.after(now)) {
                            daysToAdding = 0
                            break
                        }
                    } else {
                        daysToAdding = i
                        break
                    }
                }
            }

            if (daysToAdding == -1) {
                // Should never happen, fallback to tomorrow
                if (alarmTime.before(now)) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                }
                return alarmTime.timeInMillis
            }

            if (daysToAdding > 0) {
                alarmTime.add(Calendar.DAY_OF_YEAR, daysToAdding)
            }
            return alarmTime.timeInMillis
        }
    }
}
