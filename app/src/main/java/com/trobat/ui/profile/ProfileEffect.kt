package com.trobat.ui.profile

sealed interface ProfileEffect {
    object NavigateToLogin : ProfileEffect
}
