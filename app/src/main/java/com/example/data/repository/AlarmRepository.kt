package com.example.data.repository

import com.example.data.database.AlarmDao
import com.example.data.model.Alarm
import com.example.data.model.AlarmStats
import com.example.data.model.CustomChallenge
import com.example.util.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    val allCustomChallenges: Flow<List<CustomChallenge>> = alarmDao.getAllCustomChallenges()
    val statistics: Flow<AlarmStats> = alarmDao.getStatsFlow().map { it ?: AlarmStats() }

    suspend fun getAlarmById(id: Int): Alarm? = alarmDao.getAlarmById(id)

    suspend fun insertAlarm(alarm: Alarm) {
        val id = alarmDao.insertAlarm(alarm).toInt()
        val savedAlarm = alarm.copy(id = id)
        if (savedAlarm.enabled) {
            alarmScheduler.schedule(savedAlarm)
        } else {
            alarmScheduler.cancel(savedAlarm)
        }
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
        if (alarm.enabled) {
            alarmScheduler.schedule(alarm)
        } else {
            alarmScheduler.cancel(alarm)
        }
    }

    suspend fun toggleAlarm(alarm: Alarm) {
        val updated = alarm.copy(enabled = !alarm.enabled)
        updateAlarm(updated)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmScheduler.cancel(alarm)
        alarmDao.deleteAlarm(alarm)
    }

    // Custom challenges (Extreme Mode)
    suspend fun insertCustomChallenge(challenge: CustomChallenge): Int {
        return alarmDao.insertCustomChallenge(challenge).toInt()
    }

    suspend fun deleteCustomChallenge(challenge: CustomChallenge) {
        alarmDao.deleteCustomChallenge(challenge)
    }

    suspend fun getCustomChallengeById(id: Int): CustomChallenge? {
        return alarmDao.getCustomChallengeById(id)
    }

    // Statistics updater helpers
    suspend fun recordCompletion() {
        val currentStats = alarmDao.getStats() ?: AlarmStats()
        val now = System.currentTimeMillis()
        
        // Calculate Streak:
        // Consecutive if completion is within 36 hours of the last completion
        var newStreak = currentStats.streakCount
        val diffMs = now - currentStats.lastCompletionTime
        val consecutiveThreshold = 36 * 60 * 60 * 1000L // 36 hours

        if (currentStats.lastCompletionTime == 0L) {
            newStreak = 1
        } else if (diffMs < consecutiveThreshold) {
            newStreak += 1
        } else {
            newStreak = 1 // reset
        }

        val totalAlarmsAttempted = currentStats.completedCount + currentStats.snoozeCount + 1
        val updatedRates = if (totalAlarmsAttempted > 0) {
            (currentStats.completedCount + 1).toFloat() / totalAlarmsAttempted * 100f
        } else {
            100f
        }

        val updated = currentStats.copy(
            completedCount = currentStats.completedCount + 1,
            streakCount = newStreak,
            successRate = updatedRates,
            lastCompletionTime = now
        )
        alarmDao.updateStats(updated)
    }

    suspend fun recordSnooze() {
        val currentStats = alarmDao.getStats() ?: AlarmStats()
        val totalAlarmsAttempted = currentStats.completedCount + currentStats.snoozeCount + 1
        val updatedRates = if (totalAlarmsAttempted > 0) {
            currentStats.completedCount.toFloat() / totalAlarmsAttempted * 100f
        } else {
            0f
        }

        val updated = currentStats.copy(
            snoozeCount = currentStats.snoozeCount + 1,
            successRate = updatedRates
        )
        alarmDao.updateStats(updated)
    }
}
