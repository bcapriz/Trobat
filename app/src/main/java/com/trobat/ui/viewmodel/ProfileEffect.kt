package com.trobat.ui.viewmodel

sealed interface ProfileEffect {
    object NavigateToLogin : ProfileEffect
}
