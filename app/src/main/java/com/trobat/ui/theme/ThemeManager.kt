package com.trobat.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun init(enabled: Boolean) {
        _darkMode.value = enabled
    }

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
    }
}
