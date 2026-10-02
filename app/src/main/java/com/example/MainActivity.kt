package com.example

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AlarmViewModel
import com.example.viewmodel.AlarmViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Fetch instances from Application context
                val app = application as AlarmQuestApplication
                val vmFactory = AlarmViewModelFactory(app, app.repository)
                val mainViewModel: AlarmViewModel = viewModel(factory = vmFactory)

                val navController = rememberNavController()

                // --- Exact-alarm permission gate (Android 12+) ---
                // Without SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM, AlarmManager silently
                // falls back to inexact alarms, and Android 12+ then blocks the foreground
                // service start from the alarm broadcast -> the alarm never rings.
                // This gate forces the user through the system settings screen.
                val context = LocalContext.current
                var showExactAlarmDialog by remember { mutableStateOf(false) }

                fun refreshExactAlarmPermission() {
                    showExactAlarmDialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        !am.canScheduleExactAlarms()
                    } else {
                        false
                    }
                }

                DisposableEffect(Unit) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) refreshExactAlarmPermission()
                    }
                    val lifecycle = (context as ComponentActivity).lifecycle
                    lifecycle.addObserver(observer)
                    onDispose { lifecycle.removeObserver(observer) }
                }

                if (showExactAlarmDialog) {
                    AlertDialog(
                        onDismissRequest = { /* must act, not dismiss */ },
                        title = { Text("Enable Alarms & Reminders") },
                        text = {
                            Text(
                                "AlarmQuest needs the \"Alarms & reminders\" system permission to ring on time. " +
                                "Without it, your alarms will NOT sound. Tap below to enable it in Settings."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    try {
                                        context.startActivity(
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        )
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Failed to open exact alarm settings", e)
                                        context.startActivity(
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        )
                                    }
                                }
                            }) {
                                Text("OPEN SETTINGS")
                            }
                        }
                    )
                }

                // High priority: Reactively capture if there is an alarm actively sounding
                val ringingId by mainViewModel.ringingAlarmId.collectAsStateWithLifecycle()

                LaunchedEffect(ringingId) {
                    val targetId = ringingId
                    if (targetId != null) {
                        Log.d("MainActivity", "Alarm active! Intercepting UI navigation to ringing overlay for ID: $targetId")
                        // Clear backstack and lock user in the Ringing Screen
                        navController.navigate("alarm_ringing") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        // After alarm triggers are dismissed or resolved, route back nicely
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute == "alarm_ringing" || currentRoute == "camera_verification") {
                            Log.d("MainActivity", "Alarm stopped! Directing back to home dashboard.")
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Navigation Graph Router and Transitions declarations
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // 1. Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Head Alarms List Dashboard
                        composable("home") {
                            HomeScreen(
                                viewModel = mainViewModel,
                                onNavigateToCreate = {
                                    navController.navigate("create_alarm")
                                },
                                onNavigateToEdit = { alarmId ->
                                    navController.navigate("create_alarm?alarmId=$alarmId")
                                },
                                onNavigateToStats = {
                                    navController.navigate("statistics")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        // 3. Challenge Configuration form (Supports Add / Edit flows)
                        composable(
                            route = "create_alarm?alarmId={alarmId}",
                            arguments = listOf(
                                navArgument("alarmId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val alarmIdStr = backStackEntry.arguments?.getString("alarmId")
                            val alarmIdInt = alarmIdStr?.toIntOrNull()

                            CreateAlarmScreen(
                                viewModel = mainViewModel,
                                existingAlarmId = alarmIdInt,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 4. Immersive Fullscreen Alert Overlay
                        composable("alarm_ringing") {
                            AlarmRingingScreen(
                                viewModel = mainViewModel,
                                onNavigateToCameraChallenge = { alarmId ->
                                    navController.navigate("camera_verification")
                                }
                            )
                        }

                        // 5. Camera Preview Verification Controller
                        composable("camera_verification") {
                            CameraVerificationScreen(
                                viewModel = mainViewModel,
                                alarmId = ringingId ?: -1,
                                onNavigateBack = {
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 6. Statistics Metrics View
                        composable("statistics") {
                            StatisticsScreen(
                                viewModel = mainViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 7. Settings & Multi-Object 도전 Register Panel
                        composable("settings") {
                            SettingsScreen(
                                viewModel = mainViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
