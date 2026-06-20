package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    viewModel: AlarmViewModel,
    existingAlarmId: Int?,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Initialize fields based on whether we are editing or creating
    LaunchedEffect(existingAlarmId) {
        if (existingAlarmId != null) {
            val alarms = viewModel.allAlarms.value
            val match = alarms.find { it.id == existingAlarmId }
            if (match != null) {
                viewModel.loadAlarmIntoEdit(match)
            }
        } else {
            viewModel.prepNewAlarm()
        }
    }

    // Reactive states from ViewModel
    val hour by viewModel.editHour.collectAsStateWithLifecycle()
    val minute by viewModel.editMinute.collectAsStateWithLifecycle()
    val label by viewModel.editLabel.collectAsStateWithLifecycle()
    val repeatDays by viewModel.editRepeatDays.collectAsStateWithLifecycle()
    val snoozeMinutes by viewModel.editSnoozeMinutes.collectAsStateWithLifecycle()
    val difficulty by viewModel.editDifficulty.collectAsStateWithLifecycle()
    val customChallengesList by viewModel.customChallenges.collectAsStateWithLifecycle()

    var showHourDialog by remember { mutableStateOf(false) }
    var hourInputString by remember(hour) { mutableStateOf(String.format("%02d", hour)) }
    var minuteInputString by remember(minute) { mutableStateOf(String.format("%02d", minute)) }

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingAlarmId != null) "EDIT QUEST" else "CREATE QUEST",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NothingWhite,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(
                            "CANCEL",
                            color = NothingWhite.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NothingBlack),
                actions = {
                    TextButton(
                        onClick = {
                            val targetHour = hourInputString.toIntOrNull() ?: 7
                            val targetMinute = minuteInputString.toIntOrNull() ?: 0
                            
                            // Clamp values to safety
                            viewModel.editHour.value = targetHour.coerceIn(0, 23)
                            viewModel.editMinute.value = targetMinute.coerceIn(0, 59)
                            
                            viewModel.saveAlarm(existingAlarmId)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("save_alarm_button")
                    ) {
                        Text(
                            "SAVE",
                            color = NothingWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            // 1. Digital Time Input Custom Grid
            Text(
                "TAP DIGITS TO EDIT TIME",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Input Card
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(NothingGreyDark, RoundedCornerShape(24.dp))
                        .border(1.dp, NothingGreyMedium, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = hourInputString,
                        onValueChange = { input ->
                            if (input.length <= 2 && input.all { it.isDigit() }) {
                                hourInputString = input
                            }
                        },
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            color = NothingWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(80.dp).testTag("hour_input_field")
                    )
                }

                Text(
                    text = ":",
                    color = NothingWhite,
                    fontSize = 44.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontFamily = FontFamily.Monospace
                )

                // Minute Input Card
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(NothingGreyDark, RoundedCornerShape(24.dp))
                        .border(1.dp, NothingGreyMedium, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = minuteInputString,
                        onValueChange = { input ->
                            if (input.length <= 2 && input.all { it.isDigit() }) {
                                minuteInputString = input
                            }
                        },
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            color = NothingWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(80.dp).testTag("minute_input_field")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Alarm Label Input
            Text(
                "ALARM LABEL",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = label,
                onValueChange = { viewModel.editLabel.value = it },
                placeholder = { Text("E.g., Morning Walk", color = NothingWhite.copy(alpha = 0.3f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input"),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = NothingWhite,
                    unfocusedTextColor = NothingWhite,
                    focusedContainerColor = NothingGreyDark,
                    unfocusedContainerColor = NothingGreyDark,
                    focusedIndicatorColor = NothingWhite,
                    unfocusedIndicatorColor = NothingGreyMedium
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Repeat Week Days Selectors
            Text(
                "REPEAT ON DAYS",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..7) {
                    val isSelected = repeatDays.contains(i)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) NothingWhite else NothingGreyDark)
                            .border(1.dp, if (isSelected) NothingWhite else NothingGreyMedium, CircleShape)
                            .clickable { viewModel.toggleRepeatDay(i) }
                            .testTag("day_button_$i"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNames[i - 1],
                            color = if (isSelected) NothingBlack else NothingWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Mission Difficulty Selector (Easy, Medium, Hard, Extreme)
            Text(
                "AI MISSION DIFFICULTY",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val difficulties = listOf("Easy", "Medium", "Hard", "Extreme")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                difficulties.forEach { diff ->
                    val isSelected = difficulty.equals(diff, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) NothingWhite else NothingGreyDark)
                            .border(1.dp, if (isSelected) NothingWhite else NothingGreyMedium, RoundedCornerShape(16.dp))
                            .clickable { viewModel.editDifficulty.value = diff }
                            .padding(vertical = 12.dp)
                            .testTag("difficulty_button_$diff"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diff.uppercase(),
                            color = if (isSelected) NothingBlack else NothingWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Extreme mode details check
            if (difficulty.equals("Extreme", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NothingGreyDark, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Extreme Mode Details",
                        tint = NothingRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (customChallengesList.isEmpty()) {
                            "Warning: You haven't registered any custom objects yet under Settings! We will play standard Medium challenge."
                        } else {
                            "Extreme triggers! We will challenge you using one of your custom registered household objects."
                        },
                        fontSize = 11.sp,
                        color = if (customChallengesList.isEmpty()) NothingRed else NothingWhite.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Snooze Minutes Slider Pill Selection
            Text(
                "SNOOZE LENGTH LIMIT",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val snoozes = listOf(5, 10, 15, 20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                snoozes.forEach { minOption ->
                    val isSelected = snoozeMinutes == minOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) NothingWhite else NothingGreyDark)
                            .border(1.dp, if (isSelected) NothingWhite else NothingGreyMedium, RoundedCornerShape(16.dp))
                            .clickable { viewModel.editSnoozeMinutes.value = minOption }
                            .padding(vertical = 12.dp)
                            .testTag("snooze_button_$minOption"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$minOption MIN",
                            color = if (isSelected) NothingBlack else NothingWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
