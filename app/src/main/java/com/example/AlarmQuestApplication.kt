package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.AlarmRepository
import com.example.util.AlarmScheduler
import com.example.util.DailyResetStreakWorker

class AlarmQuestApplication : Application() {

    lateinit var database: AppDatabase
    lateinit var scheduler: AlarmScheduler
    lateinit var repository: AlarmRepository

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Room Database
        database = AppDatabase.getDatabase(this)
        
        // Initialize Alarm Scheduler
        scheduler = AlarmScheduler(this)
        
        // Initialize Repository
        repository = AlarmRepository(database.alarmDao(), scheduler)
        
        // Enqueue WorkManager periodic check
        DailyResetStreakWorker.scheduleDailyStreakCheck(this)
    }
}
