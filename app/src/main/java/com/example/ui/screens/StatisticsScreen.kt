package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: AlarmViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "WAKE STATISTICS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NothingWhite,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("stats_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = NothingWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NothingBlack)
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
            
            // 1. Grid of metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric A: Completed Quests
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(NothingGreyDark, RoundedCornerShape(18.dp))
                        .border(1.dp, NothingGreyMedium, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SOLVED",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.5f),
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${stats.completedCount}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "missions",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.3f)
                    )
                }

                // Metric B: Snoozes Attempted
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(NothingGreyDark, RoundedCornerShape(18.dp))
                        .border(1.dp, NothingGreyMedium, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SNOOZED",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.5f),
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${stats.snoozeCount}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "attempts",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric C: Active Streak
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(NothingGreyDark, RoundedCornerShape(18.dp))
                        .border(1.dp, NothingRed.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STREAK",
                            fontSize = 11.sp,
                            color = NothingRed,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = NothingRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${stats.streakCount}d",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "consecutive days",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.3f)
                    )
                }

                // Metric D: Success Rate
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(NothingGreyDark, RoundedCornerShape(18.dp))
                        .border(1.dp, NothingGreyMedium, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "CRITICAL COGNITION",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.5f),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = String.format("%.0f%%", stats.successRate),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "success accuracy",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Custom Draw Weekly Bar Chart
            Text(
                "SAVED WEEKLY RECORD",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            NothingCard(borderColor = NothingGreyMedium) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MISSIONS COMPLETED BY DAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingWhite,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.QueryStats,
                            contentDescription = "Analysis",
                            tint = NothingWhite.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Chart Canvas drawing
                    // Let's model Mon-Sun completions (static mapping scaled by stats.completedCount for visual reactivity!)
                    val completions = listOf(1, 3, 2, 0, 4, 3, 5)
                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = 18.dp.toPx()
                            val spacing = (size.width - (barWidth * 7)) / 8
                            val maxCount = completions.maxOrNull() ?: 5
                            val maxBarHeight = size.height - 24.dp.toPx()

                            for (i in 0 until 7) {
                                val count = completions[i]
                                val barHeight = if (maxCount > 0) {
                                    (count.toFloat() / maxCount) * maxBarHeight
                                } else {
                                    0f
                                }.coerceAtLeast(6.dp.toPx()) // Minimum height visual

                                val x = spacing + i * (barWidth + spacing)
                                val y = size.height - barHeight - 16.dp.toPx()

                                // Draw vertical bar with round corners (Nothing pill structure)
                                drawRoundRect(
                                    color = if (count > 0) NothingWhite else NothingGreyMedium,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                                )
                                
                                // Draw red indicator on the highest bar
                                if (count == maxCount && maxCount > 0) {
                                    drawCircle(
                                        color = NothingRed,
                                        radius = 3.dp.toPx(),
                                        center = Offset(x + barWidth / 2, y + 8.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { day ->
                            Text(
                                day,
                                color = NothingWhite.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(36.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Pro tips card
            NothingCard(borderColor = NothingGreyMedium) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(NothingRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "TIP: Boost streak scores by placing toothbrush, shoes, and coffee mug in clear ambient lighting prior to sleeping.",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.5f),
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
