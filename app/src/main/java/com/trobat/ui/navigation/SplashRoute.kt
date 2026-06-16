package com.trobat.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trobat.ui.screen.LoadingScreen
import com.trobat.ui.screen.SplashScreen
import com.trobat.ui.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashRoute(
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        delay(1800)
        viewModel.showLoadingScreen()
        delay(1800)
        onNavigateToHome()
    }

    AnimatedContent(
        targetState = uiState.showLoading,
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = 700)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 700))
        },
        label = "Splash to Loading transition"
    ) { showLoading ->
        if (showLoading) LoadingScreen() else SplashScreen()
    }
}
