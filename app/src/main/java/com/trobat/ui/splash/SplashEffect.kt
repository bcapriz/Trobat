package com.trobat.ui.splash

sealed interface SplashEffect {
    object NavigateToMain : SplashEffect
    object NavigateToLogin : SplashEffect
    object NavigateToOnboarding : SplashEffect
}
