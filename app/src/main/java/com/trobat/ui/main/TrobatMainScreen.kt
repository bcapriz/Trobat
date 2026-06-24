package com.trobat.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trobat.data.repository.AppContainer
import com.trobat.ui.capture.CaptureEvidenceScreen
import com.trobat.ui.home.CitizenHomeScreen
import com.trobat.ui.heatmap.HeatMapScreen
import com.trobat.ui.navigation.BottomRoutes
import com.trobat.ui.navigation.TrobatBottomBar
import com.trobat.ui.navigation.MainRoutes
import com.trobat.ui.notifications.NotificationsScreen
import com.trobat.ui.profile.ProfileScreen
import com.trobat.ui.report.ConfirmReportScreen


@Composable
fun TrobatMainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val onboardingPrefs = remember { AppContainer.onboardingPrefs }
    val coachmarkController = remember {
        if (!onboardingPrefs.hasSeenCoachmarks) CoachmarkController() else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            TrobatBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (currentRoute != route) {
                        val goingToStart = route == BottomRoutes.CASES
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = !goingToStart
                            }
                            launchSingleTop = true
                            restoreState = !goingToStart
                        }
                    }
                },
                onCameraClick = {
                    if (!AppContainer.reportDraftPrefs.isEmpty()) {
                        navController.navigate(MainRoutes.CONFIRM_REPORT)
                    } else {
                        navController.navigate(BottomRoutes.CAMERA) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                coachmarkController = coachmarkController
            )
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = BottomRoutes.CASES,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(BottomRoutes.CASES) {
                CitizenHomeScreen(
                    onOpenMap = {
                        navController.navigate(BottomRoutes.HEATMAP)
                    },
                    onCaptureEvidence = {
                        navController.navigate(BottomRoutes.CAMERA)
                    },
                    onResumeDraft = {
                        navController.navigate(MainRoutes.CONFIRM_REPORT)
                    }
                )
            }

            composable(BottomRoutes.HEATMAP) {
                HeatMapScreen(
                    onNavigateToCamera = { navController.navigate(BottomRoutes.CAMERA) }
                )
            }

            composable(BottomRoutes.CAMERA) {
                CaptureEvidenceScreen(
                    onConfirmReport = {
                        navController.navigate(MainRoutes.CONFIRM_REPORT)
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(MainRoutes.CONFIRM_REPORT) {
                ConfirmReportScreen(
                    onSendReport = {
                        navController.navigate(BottomRoutes.HEATMAP) {
                            popUpTo(BottomRoutes.CAMERA) { inclusive = true }
                        }
                    },
                    onRetakePhoto = {
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.navigate(BottomRoutes.CASES) {
                            popUpTo(BottomRoutes.CAMERA) { inclusive = true }
                        }
                    }
                )
            }

            composable(BottomRoutes.NOTIFICATIONS) {
                NotificationsScreen()
            }

            composable(BottomRoutes.PROFILE) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }

    coachmarkController?.let { controller ->
        if (controller.currentStep != CoachmarkStep.DONE) {
            CoachmarkOverlay(
                controller = controller,
                onDismiss = {
                    onboardingPrefs.hasSeenCoachmarks = true
                    controller.dismiss()
                }
            )
        }
    }
    } // Box
}

