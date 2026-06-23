package com.trobat.ui.viewmodel

sealed interface LoginEffect {
    data object NavigateToMain : LoginEffect
    data object NavigateToRegister : LoginEffect
}
