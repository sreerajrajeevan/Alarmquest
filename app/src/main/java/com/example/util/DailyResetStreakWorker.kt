package com.example.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmStats
import java.util.concurrent.TimeUnit

class DailyResetStreakWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DailyResetStreakWorker", "Daily streak checker running...")
        val db = AppDatabase.getDatabase(applicationContext)
        val statsDao = db.alarmDao()

        try {
            val stats = statsDao.getStats() ?: AlarmStats()
            val now = System.currentTimeMillis()
            val lastCompletion = stats.lastCompletionTime

            if (lastCompletion > 0L) {
                val diffMs = now - lastCompletion
                val maxMs = 36 * 60 * 60 * 1000L // 36 hours max window to keep streak alive
                
                if (diffMs > maxMs && stats.streakCount > 0) {
                    Log.d("DailyResetStreakWorker", "Resetting streak from ${stats.streakCount} to 0 due to inactivity.")
                    val updated = stats.copy(streakCount = 0)
                    statsDao.updateStats(updated)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("DailyResetStreakWorker", "Error validating user streaks in background", e)
            return Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "DAILY_STREAK_STABILIZER"

        fun scheduleDailyStreakCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<DailyResetStreakWorker>(
                1, TimeUnit.DAYS
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d("DailyResetStreakWorker", "Periodic WorkManager challenge enqueued successfully.")
        }
    }
}
