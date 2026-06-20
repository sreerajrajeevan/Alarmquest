package com.example.data.database

import androidx.room.*
import com.example.data.model.Alarm
import com.example.data.model.AlarmStats
import com.example.data.model.CustomChallenge
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE enabled = 1")
    suspend fun getEnabledAlarms(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    // Custom challenges (Extreme Difficulty)
    @Query("SELECT * FROM custom_challenges ORDER BY id DESC")
    fun getAllCustomChallenges(): Flow<List<CustomChallenge>>

    @Query("SELECT * FROM custom_challenges WHERE id = :id")
    suspend fun getCustomChallengeById(id: Int): CustomChallenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomChallenge(challenge: CustomChallenge): Long

    @Delete
    suspend fun deleteCustomChallenge(challenge: CustomChallenge)

    // Statistics
    @Query("SELECT * FROM statistics WHERE id = 1 LIMIT 1")
    fun getStatsFlow(): Flow<AlarmStats?>

    @Query("SELECT * FROM statistics WHERE id = 1 LIMIT 1")
    suspend fun getStats(): AlarmStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStats(stats: AlarmStats)
}

@Database(entities = [Alarm::class, CustomChallenge::class, AlarmStats::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alarm_quest_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
