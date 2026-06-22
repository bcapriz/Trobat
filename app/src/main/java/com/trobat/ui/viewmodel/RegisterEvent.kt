package com.trobat.ui.viewmodel

sealed interface RegisterEvent {
    data class NameChanged(val value: String) : RegisterEvent
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class NationalIdChanged(val value: String) : RegisterEvent
    data class PhoneChanged(val value: String) : RegisterEvent
    data object RegisterClicked : RegisterEvent
    data object LoginClicked : RegisterEvent
}
