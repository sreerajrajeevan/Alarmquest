package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
