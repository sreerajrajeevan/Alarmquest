package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Alarm
import com.example.ui.components.*
import com.example.util.AlarmScheduler
import com.example.viewmodel.AlarmViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AlarmViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val alarms by viewModel.allAlarms.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()

    // Calculation for active alarm remaining time
    val nextAlarmInfo = remember(alarms) {
        calculateNextAlarmRemainingText(alarms)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "ALARMQUEST",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = NothingWhite,
                                    fontSize = 20.sp,
                                    letterSpacing = 4.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "NOTHING EDITION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NothingRed,
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NothingBlack
                ),
                actions = {
                    IconButton(
                        onClick = onNavigateToStats,
                        modifier = Modifier.testTag("stats_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = "Statistics",
                            tint = NothingWhite
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = NothingWhite
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.prepNewAlarm()
                    onNavigateToCreate()
                },
                containerColor = NothingWhite,
                contentColor = NothingBlack,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("add_alarm_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Alarm",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            
            // Hero Widget displaying the Next Scheduled Alarm time
            Spacer(modifier = Modifier.height(16.dp))
            NothingCard(borderColor = NothingGreyMedium) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UPCOMING CHALLENGE",
                            fontSize = 11.sp,
                            color = NothingWhite.copy(alpha = 0.5f),
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = nextAlarmInfo,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NothingWhite,
                            lineHeight = 24.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(NothingGreyMedium, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Alarm Clock Icon",
                            tint = NothingWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Streak tracker small bar
            if (statistics.streakCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NothingGreyDark, RoundedCornerShape(12.dp))
                        .border(1.dp, NothingRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = NothingRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STREAK CHALLENGE ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingWhite,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "${statistics.streakCount} DAYS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                "SAVED ALARMS",
                fontSize = 12.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic Alarms List
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsOff,
                            contentDescription = "No Alarms",
                            tint = NothingGreyMedium,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "NO ALARMS CREATED",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingWhite.copy(alpha = 0.6f),
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Tap + to set up a wake up quest",
                            fontSize = 12.sp,
                            color = NothingWhite.copy(alpha = 0.3f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            onToggleEnabled = { viewModel.toggleAlarmEnabled(alarm) },
                            onEdit = { onNavigateToEdit(alarm.id) },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmRow(
    alarm: Alarm,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedActions by remember { mutableStateOf(false) }

    NothingCard(
        borderColor = if (alarm.enabled) NothingWhite.copy(alpha = 0.2f) else NothingGreyMedium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedActions = !expandedActions }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Time block
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.enabled) NothingWhite else NothingWhite.copy(alpha = 0.3f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alarm.label.ifEmpty { "Quest Alarm" },
                        fontSize = 14.sp,
                        color = if (alarm.enabled) NothingWhite else NothingWhite.copy(alpha = 0.3f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = alarm.getRepeatDaysDisplay() + " • Mode: " + alarm.difficulty,
                        fontSize = 11.sp,
                        color = if (alarm.enabled) NothingRed else NothingWhite.copy(alpha = 0.3f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NothingBlack,
                        checkedTrackColor = NothingWhite,
                        uncheckedThumbColor = NothingGreyLight,
                        uncheckedTrackColor = NothingGreyDark,
                        uncheckedBorderColor = NothingGreyMedium
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            AnimatedVisibility(
                visible = expandedActions,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    NothingDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_alarm_${alarm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = NothingRed
                            )
                        }
                        
                        Row {
                            TextButton(onClick = onEdit) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit",
                                        tint = NothingWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "EDIT CHALLENGE",
                                        color = NothingWhite,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Utility to calculate hours and minutes remaining until closest active alarm
private fun calculateNextAlarmRemainingText(alarms: List<Alarm>): String {
    val enabledAlarms = alarms.filter { it.enabled }
    if (enabledAlarms.isEmpty()) return "NO ACTIVE QUESTS"

    val now = Calendar.getInstance()
    var minDiffMs = Long.MAX_VALUE
    var closestAlarm: Alarm? = null

    for (alarm in enabledAlarms) {
        val triggerTimeMs = AlarmScheduler.calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.getRepeatDaysList())
        val diffMs = triggerTimeMs - now.timeInMillis
        if (diffMs > 0 && diffMs < minDiffMs) {
            minDiffMs = diffMs
            closestAlarm = alarm
        }
    }

    if (closestAlarm == null) return "NO ACTIVE QUESTS"

    val diffMin = (minDiffMs / (1000 * 60)) % 60
    val diffHours = (minDiffMs / (1000 * 60 * 60)) % 24
    val diffDays = minDiffMs / (1000 * 60 * 60 * 24)

    val labelStr = closestAlarm.label.ifEmpty { "Quest" }
    
    return buildString {
        append("Rings in ")
        if (diffDays > 0) append("${diffDays}d ")
        if (diffHours > 0 || diffDays > 0) append("${diffHours}h ")
        append("${diffMin}m")
        append("\nTarget: ")
        append(if (closestAlarm.difficulty == "Extreme") "Custom Target" else closestAlarm.difficulty)
    }
}
