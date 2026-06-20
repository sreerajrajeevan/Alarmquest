package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Alarm
import com.example.data.model.AlarmStats
import com.example.data.model.CustomChallenge
import com.example.data.model.ObjectPool
import com.example.data.repository.AlarmRepository
import com.example.service.AlarmService
import com.example.util.ObjectVerifier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AlarmViewModel(
    application: Application,
    private val repository: AlarmRepository
) : AndroidViewModel(application) {

    // 1. Core Data sources flowing from Repository
    val allAlarms = repository.allAlarms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customChallenges = repository.allCustomChallenges.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statistics = repository.statistics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlarmStats()
    )

    // 2. Active Alarm Ringing state (links with our Service)
    val ringingAlarmId: StateFlow<Int?> = AlarmService.ringingAlarmId
    
    private val _ringingAlarm = MutableStateFlow<Alarm?>(null)
    val ringingAlarm: StateFlow<Alarm?> = _ringingAlarm.asStateFlow()

    // 3. Challenge state during verification
    private val _targetChallengeObject = MutableStateFlow<String>("")
    val targetChallengeObject: StateFlow<String> = _targetChallengeObject.asStateFlow()

    private val _isCustomChallengeRinging = MutableStateFlow(false)
    private val _customChallengeLabel = MutableStateFlow("")

    private val _verificationState = MutableStateFlow<VerificationUIState>(VerificationUIState.Idle)
    val verificationState: StateFlow<VerificationUIState> = _verificationState.asStateFlow()

    // 4. Alarm Creation/Editing State (MVVM boundary)
    val editHour = MutableStateFlow(7)
    val editMinute = MutableStateFlow(0)
    val editLabel = MutableStateFlow("")
    val editRepeatDays = MutableStateFlow<List<Int>>(emptyList())
    val editToneUri = MutableStateFlow("")
    val editSnoozeMinutes = MutableStateFlow(5)
    val editDifficulty = MutableStateFlow("Medium")
    val editCustomTargetId = MutableStateFlow<Int?>(null)

    // 5. App Settings
    val isDarkMode = MutableStateFlow(true)
    val confidenceLevel = MutableStateFlow(0.80f)
    val vibrationEnabled = MutableStateFlow(true)
    val defaultDifficultySetting = MutableStateFlow("Medium")

    init {
        // Automatically fetch active ringing Alarm details when ringingAlarmId emits
        viewModelScope.launch {
            ringingAlarmId.collect { alarmId ->
                if (alarmId != null) {
                    val entity = repository.getAlarmById(alarmId)
                    _ringingAlarm.value = entity
                    if (entity != null) {
                        setupChallengeForAlarm(entity)
                    }
                } else {
                    _ringingAlarm.value = null
                    _verificationState.value = VerificationUIState.Idle
                }
            }
        }
    }

    // Set up a random target object based on alarm difficulty
    private suspend fun setupChallengeForAlarm(alarm: Alarm) {
        _verificationState.value = VerificationUIState.Idle
        if (alarm.difficulty.equals("Extreme", ignoreCase = true)) {
            // Pick a registered custom object challenge if exists, otherwise fallback to Medium level
            val challenges = customChallenges.value
            if (challenges.isNotEmpty()) {
                val picked = challenges.random()
                _isCustomChallengeRinging.value = true
                _customChallengeLabel.value = picked.detectedLabel
                _targetChallengeObject.value = picked.name
                Log.d("AlarmViewModel", "Selected custom extreme challenge: ${picked.name} (label: ${picked.detectedLabel})")
            } else {
                _isCustomChallengeRinging.value = false
                val obj = ObjectPool.getRandomItemForDifficulty("Medium")
                _targetChallengeObject.value = obj.name
                Log.d("AlarmViewModel", "No custom challenges registered! Fell back to: ${obj.name}")
            }
        } else {
            _isCustomChallengeRinging.value = false
            val obj = ObjectPool.getRandomItemForDifficulty(alarm.difficulty)
            _targetChallengeObject.value = obj.name
            Log.d("AlarmViewModel", "Selected pool challenge for difficulty ${alarm.difficulty}: ${obj.name}")
        }
    }

    // Initialize creation fields
    fun prepNewAlarm() {
        editHour.value = 7
        editMinute.value = 0
        editLabel.value = ""
        editRepeatDays.value = emptyList()
        editToneUri.value = ""
        editSnoozeMinutes.value = 5
        editDifficulty.value = defaultDifficultySetting.value
        editCustomTargetId.value = null
    }

    fun loadAlarmIntoEdit(alarm: Alarm) {
        editHour.value = alarm.hour
        editMinute.value = alarm.minute
        editLabel.value = alarm.label
        editRepeatDays.value = alarm.getRepeatDaysList()
        editToneUri.value = alarm.toneUri
        editSnoozeMinutes.value = alarm.snoozeMinutes
        editDifficulty.value = alarm.difficulty
        editCustomTargetId.value = alarm.customTargetId
    }

    // Toggle repeat day selection
    fun toggleRepeatDay(dayNum: Int) {
        val currentList = editRepeatDays.value.toMutableList()
        if (currentList.contains(dayNum)) {
            currentList.remove(dayNum)
        } else {
            currentList.add(dayNum)
        }
        editRepeatDays.value = currentList.sorted()
    }

    // Save alarm (creates a new one or updates existing if ID is provided)
    fun saveAlarm(existingId: Int? = null) {
        viewModelScope.launch {
            val alarm = Alarm(
                id = existingId ?: 0,
                hour = editHour.value,
                minute = editMinute.value,
                label = editLabel.value,
                repeatDays = editRepeatDays.value.joinToString(","),
                toneUri = editToneUri.value,
                snoozeMinutes = editSnoozeMinutes.value,
                enabled = true,
                difficulty = editDifficulty.value,
                customTargetId = editCustomTargetId.value
            )
            repository.insertAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun toggleAlarmEnabled(alarm: Alarm) {
        viewModelScope.launch {
            repository.toggleAlarm(alarm)
        }
    }

    // Snooze Alarm Action (delays by snoozeMinutes and records stats)
    fun snoozeRingingAlarm() {
        val currentRingId = ringingAlarmId.value ?: return
        val currentAlarm = ringingAlarm.value ?: return
        
        viewModelScope.launch {
            repository.recordSnooze()
            // Turn off current ringing service
            AlarmService.stopAlarm(getApplication())
            
            // Re-schedule alarm for snoozeMinutes in future
            val calendar = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.MINUTE, currentAlarm.snoozeMinutes)
            }
            
            val snoozedAlarm = currentAlarm.copy(
                hour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
                minute = calendar.get(java.util.Calendar.MINUTE),
                enabled = true,
                repeatDays = "" // Force trigger only once for this snooze instance
            )
            // Schedule the temporary snoozed alarm in AlarmManager
            repository.insertAlarm(snoozedAlarm)
        }
    }

    // Manual custom challenge registration (Extreme Mode Setup)
    fun registerCustomChallenge(name: String, label: String) {
        viewModelScope.launch {
            repository.insertCustomChallenge(
                CustomChallenge(name = name, detectedLabel = label)
            )
        }
    }

    fun deleteCustomChallenge(challenge: CustomChallenge) {
        viewModelScope.launch {
            repository.deleteCustomChallenge(challenge)
        }
    }

    // Verify Captured Image Bitmaps
    fun verifyCapturedImage(bitmap: Bitmap) {
        _verificationState.value = VerificationUIState.Processing
        
        viewModelScope.launch {
            val result = ObjectVerifier.verifyImage(
                bitmap = bitmap,
                targetObjectName = _targetChallengeObject.value,
                isCustomExtreme = _isCustomChallengeRinging.value,
                customLabel = _customChallengeLabel.value,
                minConfidence = confidenceLevel.value
            )

            when (result) {
                is ObjectVerifier.VerificationResult.Success -> {
                    _verificationState.value = VerificationUIState.Success
                    // Shut down service!
                    AlarmService.stopAlarm(getApplication())
                    // Update stats
                    repository.recordCompletion()
                    Log.d("AlarmViewModel", "Verification Succeeded!")
                }
                is ObjectVerifier.VerificationResult.Failure -> {
                    _verificationState.value = VerificationUIState.Failure(result.reason)
                    Log.d("AlarmViewModel", "Verification Failed: ${result.reason}")
                }
            }
        }
    }

    fun resetVerificationUI() {
        _verificationState.value = VerificationUIState.Idle
    }
}

// Sealed state outlining the status of camera mission analysis
sealed class VerificationUIState {
    object Idle : VerificationUIState()
    object Processing : VerificationUIState()
    object Success : VerificationUIState()
    data class Failure(val error: String) : VerificationUIState()
}

// Custom factory to pass Application and Repository
class AlarmViewModelFactory(
    private val application: Application,
    private val repository: AlarmRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
