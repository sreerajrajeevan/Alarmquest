package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.viewmodel.AlarmViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun AlarmRingingScreen(
    viewModel: AlarmViewModel,
    onNavigateToCameraChallenge: (Int) -> Unit
) {
    val ringingAlarmId by viewModel.ringingAlarmId.collectAsState()
    val ringingAlarm by viewModel.ringingAlarm.collectAsState()
    val targetObject by viewModel.targetChallengeObject.collectAsState()

    // Real-time time updater
    var hour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour = cal.get(Calendar.HOUR_OF_DAY)
            minute = cal.get(Calendar.MINUTE)
            delay(1000)
        }
    }

    // Pulsing background red LED glow dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_led")
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // 1. Top status LEDs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .scale(pulseScale.value)
                    .size(12.dp)
                    .background(NothingRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ALARM RINGING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NothingRed,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // 2. Huge Dot-Matrix LED Clock Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 30.dp)
        ) {
            DotMatrixTime(
                hour = hour,
                minute = minute,
                cellSize = 12.dp,
                dotColor = NothingWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = ringingAlarm?.label?.uppercase() ?: "WAKE UP QUEST",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = NothingWhite.copy(alpha = 0.5f),
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.Monospace
                ),
                textAlign = TextAlign.Center
            )
        }

        // 3. Challenge Target Message box 
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            NothingCard(borderColor = NothingRed.copy(alpha = 0.5f)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QUEST TO SNOOZE/STOP ALARM",
                        fontSize = 11.sp,
                        color = NothingRed,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Capture a real-world photo of a:",
                        fontSize = 14.sp,
                        color = NothingWhite.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = targetObject.uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NothingWhite,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "validation requires 80% accuracy\nAnti-cheat blocks dark or blurred captures.",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.4f),
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 4. Large Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NothingButton(
                text = "START CAMERA CHALLENGE",
                onClick = {
                    ringingAlarmId?.let { id ->
                        onNavigateToCameraChallenge(id)
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("start_camera_challenge_btn")
            )

            // Snooze
            NothingButton(
                text = "SNOOZE",
                onClick = { viewModel.snoozeRingingAlarm() },
                modifier = Modifier.fillMaxWidth().testTag("snooze_alarm_btn"),
                isSecondary = true
            )
        }
    }
}
