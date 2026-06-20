package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: String, // Comma-separated integers: "1,2,3" for Mon, Tue, Wed (1-7)
    val toneUri: String,    // Alarm ringtone URI
    val snoozeMinutes: Int, // Snooze duration (e.g., 5, 10, 15)
    val enabled: Boolean,
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard", "Extreme"
    val customTargetId: Int? = null    // Link to CustomChallenge if difficulty is "Extreme"
) : Serializable {

    // Converts repeated days to a list of ints
    fun getRepeatDaysList(): List<Int> {
        if (repeatDays.isEmpty()) return emptyList()
        return repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    // Friendly display string for Repeat Days
    fun getRepeatDaysDisplay(): String {
        val days = getRepeatDaysList()
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Every day"
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return days.sorted().joinToString(", ") { dayNum ->
            if (dayNum in 1..7) dayNames[dayNum - 1] else ""
        }
    }
}

@Entity(tableName = "custom_challenges")
data class CustomChallenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,             // User-assigned name (e.g. "Bathroom Mirror")
    val detectedLabel: String,    // ML Kit detected label stored when registering (e.g. "Sink")
    val registeredDate: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "statistics")
data class AlarmStats(
    @PrimaryKey val id: Int = 1, // Singleton row
    val completedCount: Int = 0,
    val snoozeCount: Int = 0,
    val streakCount: Int = 0,
    val successRate: Float = 0f,
    val lastCompletionTime: Long = 0L // To compute streak count correctly
)
