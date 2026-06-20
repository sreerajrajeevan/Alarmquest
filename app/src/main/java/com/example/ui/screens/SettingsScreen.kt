package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomChallenge
import com.example.ui.components.*
import com.example.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AlarmViewModel,
    onNavigateBack: () -> Unit
) {
    val confidence by viewModel.confidenceLevel.collectAsStateWithLifecycle()
    val isDarkByState by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val defaultDiffSetting by viewModel.defaultDifficultySetting.collectAsStateWithLifecycle()
    val customChallenges by viewModel.customChallenges.collectAsStateWithLifecycle()

    var customNameInput by remember { mutableStateOf("") }
    var customLabelInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CORE CONFIG",
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
                        modifier = Modifier.testTag("settings_back_button")
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
            
            // 1. Theme and Vibration Toggles
            Text(
                "PREFERENCES",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            NothingCard(borderColor = NothingGreyMedium) {
                // Vibration Control Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Continuous Vibration", color = NothingWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Vibrate phone during active alarm triggers", color = NothingWhite.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { viewModel.vibrationEnabled.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingBlack,
                            checkedTrackColor = NothingWhite
                        ),
                        modifier = Modifier.testTag("vibration_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                NothingDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Dark mode fallback indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("True Black UI Theme", color = NothingWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("High contrast dark theme optimizes AMOLED battery life", color = NothingWhite.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isDarkByState,
                        onCheckedChange = { viewModel.isDarkMode.value = it },
                        enabled = false, // Force dark mode as specified by Nothing requirements
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingBlack,
                            checkedTrackColor = NothingWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. ML Settings: Confidence levels & default difficulties
            Text(
                "VERIFICATION CRITERIA (ML KIT)",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            NothingCard(borderColor = NothingGreyMedium) {
                // Confidence settings
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Accuracy Threshold", color = NothingWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${(confidence * 100).toInt()}% CONFIDENCE", color = NothingRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = confidence,
                        onValueChange = { viewModel.confidenceLevel.value = it },
                        valueRange = 0.70f..0.95f,
                        colors = SliderDefaults.colors(
                            thumbColor = NothingWhite,
                            activeTrackColor = NothingWhite,
                            inactiveTrackColor = NothingGreyMedium
                        ),
                        modifier = Modifier.testTag("confidence_slider")
                    )
                    Text(
                        "Lower scores are easy to complete but prone to cheating. Higher scores require perfect lighting and focal orientation.",
                        color = NothingWhite.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Custom objects challenges list & creation
            Text(
                "CUSTOM EXTREME OBJECTS",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            NothingCard(borderColor = NothingGreyMedium) {
                Column {
                    Text(
                        text = "REGISTER NEW HOUSEHOLD CHALLENGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Missions match the exact ML target word you register. Choose standard nouns for clean results.",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = customNameInput,
                        onValueChange = { customNameInput = it },
                        placeholder = { Text("Challenge Name (e.g., Bathroom sink)", color = NothingWhite.copy(alpha = 0.3f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_challenge_name_input"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = NothingWhite,
                            unfocusedTextColor = NothingWhite,
                            focusedContainerColor = NothingGreyDark,
                            unfocusedContainerColor = NothingGreyDark,
                            focusedIndicatorColor = NothingWhite,
                            unfocusedIndicatorColor = NothingGreyMedium
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = customLabelInput,
                        onValueChange = { customLabelInput = it },
                        placeholder = { Text("ML Target Word (e.g., Sink, Mirror, Door, Coffee)", color = NothingWhite.copy(alpha = 0.3f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_challenge_label_input"),
                        helperText = "Target noun that machine learning looks for",
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = NothingWhite,
                            unfocusedTextColor = NothingWhite,
                            focusedContainerColor = NothingGreyDark,
                            unfocusedContainerColor = NothingGreyDark,
                            focusedIndicatorColor = NothingWhite,
                            unfocusedIndicatorColor = NothingGreyMedium
                        )
                    )

                    if (validationError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(validationError, color = NothingRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    NothingButton(
                        text = "REGISTER CHALLENGE",
                        onClick = {
                            if (customNameInput.trim().isEmpty() || customLabelInput.trim().isEmpty()) {
                                validationError = "Error: Input fields cannot be left blank."
                            } else {
                                validationError = ""
                                viewModel.registerCustomChallenge(
                                    customNameInput.trim(),
                                    customLabelInput.trim().lowercase()
                                )
                                customNameInput = ""
                                customLabelInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Display quick click presets to register easily
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "QUICK PRESETS (POPULAR SENSORS)",
                        fontSize = 10.sp,
                        color = NothingWhite.copy(alpha = 0.4f),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("Mirror", "Mirror", "mirror"),
                            Triple("Coffee maker", "Coffee", "coffee maker"),
                            Triple("Kitchen Sink", "Sink", "sink")
                        ).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, NothingGreyMedium, RoundedCornerShape(12.dp))
                                    .clickable {
                                        customNameInput = preset.first
                                        customLabelInput = preset.third
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(preset.second.uppercase(), color = NothingWhite, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    NothingDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Registered items list
                    Text(
                        "REGISTERED HOUSEHOLD TARGETS",
                        fontSize = 11.sp,
                        color = NothingWhite.copy(alpha = 0.4f),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (customChallenges.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Warning, "Warning", tint = NothingRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("No custom targets saved. Extreme mode alarms will run in default Medium difficulty mode.", color = NothingWhite.copy(alpha = 0.4f), fontSize = 11.sp)
                        }
                    } else {
                        customChallenges.forEach { challenge ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(challenge.name, color = NothingWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("ML matching target: '${challenge.detectedLabel}'", color = NothingWhite.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteCustomChallenge(challenge) },
                                    modifier = Modifier.testTag("delete_challenge_${challenge.id}")
                                ) {
                                    Icon(Icons.Filled.Delete, "Delete", tint = NothingRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    helperText: String = "",
    colors: TextFieldColors
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = modifier,
            colors = colors,
            shape = RoundedCornerShape(12.dp)
        )
        if (helperText.isNotEmpty()) {
            Text(
                text = helperText,
                fontSize = 10.sp,
                color = NothingWhite.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 2.dp, start = 4.dp),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
