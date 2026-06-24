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
import com.trobat.ui.splash.LoadingScreen
import com.trobat.ui.splash.SplashEffect
import com.trobat.ui.splash.SplashScreen
import com.trobat.ui.splash.SplashViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashRoute(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startSplash()
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigateToMain -> onNavigateToMain()
                SplashEffect.NavigateToLogin -> onNavigateToLogin()
                SplashEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
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
