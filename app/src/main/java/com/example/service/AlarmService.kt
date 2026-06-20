package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.data.database.AlarmDao
import com.example.data.model.Alarm
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var alarmDao: AlarmDao? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var volumeRampHandler: Handler? = null
    private var volumeRampRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        alarmDao = AppDatabase.getDatabase(this).alarmDao()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        Log.d("AlarmService", "Starting foreground alarm service for ID: $alarmId")

        if (alarmId == -1) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Fetch alarm and proceed
        serviceScope.launch {
            val alarm = alarmDao?.getAlarmById(alarmId)
            if (alarm != null) {
                activeAlarm = alarm
                _ringingAlarmId.value = alarmId
                startRinging(alarm)
            } else {
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startRinging(alarm: Alarm) {
        // Create high-importance Notification Channel
        val channelId = "alarm_quest_channel"
        createNotificationChannel(channelId)

        // Intent to launch MainActivity in ringing mode
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("RINGING_ALARM_ID", alarm.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alarm.id,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ALARM RINGING")
            .setContentText(alarm.label.ifEmpty { "Quest to Wake Up" })
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .build()

        // Start Foreground Service
        startForeground(NOTIFICATION_ID, notification)

        // Play Sound
        try {
            val soundUri = if (alarm.toneUri.isNotEmpty()) {
                Uri.parse(alarm.toneUri)
            } else {
                val systemDefault = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                systemDefault ?: android.provider.Settings.System.DEFAULT_RINGTONE_URI
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                setVolume(0.1f, 0.1f) // Start low for potential ramp-up
                start()
            }

            // Implement Ramp Up Volume
            rampVolumeUp()

        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to setup media player", e)
        }

        // Start Vibrate
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    val pattern = longArrayOf(0, 1000, 1000) // Vibrate 1s, pause 1s
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(pattern, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(pattern, 0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to trigger vibrator", e)
        }
    }

    private fun rampVolumeUp() {
        val maxSteps = 10
        var currentStep = 1
        volumeRampHandler = Handler(Looper.getMainLooper())
        volumeRampRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val volume = (currentStep.toFloat() / maxSteps)
                        mp.setVolume(volume, volume)
                        if (currentStep < maxSteps) {
                            currentStep++
                            volumeRampHandler?.postDelayed(this, 3000) // Increase every 3s
                        }
                    }
                }
            }
        }
        volumeRampHandler?.post(volumeRampRunnable!!)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AlarmQuest Wake Alarms"
            val descriptionText = "Triggers alarm challenge alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                setBypassDnd(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        
        volumeRampHandler?.removeCallbacksAndMessages(null)
        
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AlarmService", "Error releasing media player", e)
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping vibrator", e)
        }

        activeAlarm = null
        _ringingAlarmId.value = null
        Log.d("AlarmService", "AlarmService terminated")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 50797
        
        var activeAlarm: Alarm? = null

        private val _ringingAlarmId = MutableStateFlow<Int?>(null)
        val ringingAlarmId: StateFlow<Int?> = _ringingAlarmId

        fun stopAlarm(context: Context) {
            val stopIntent = Intent(context, AlarmService::class.java)
            context.stopService(stopIntent)
        }
    }
}
