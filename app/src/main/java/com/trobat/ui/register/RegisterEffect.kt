package com.trobat.ui.register

sealed interface RegisterEffect {
    data object NavigateToLogin : RegisterEffect
}
