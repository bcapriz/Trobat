package com.trobat.ui.viewmodel

sealed interface RegisterEffect {
    data object NavigateToLogin : RegisterEffect
}
