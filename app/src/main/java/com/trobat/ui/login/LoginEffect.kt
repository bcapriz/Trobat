package com.trobat.ui.login

sealed interface LoginEffect {
    data object NavigateToMain : LoginEffect
    data object NavigateToRegister : LoginEffect
}
