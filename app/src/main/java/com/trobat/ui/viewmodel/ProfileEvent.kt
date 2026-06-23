package com.trobat.ui.viewmodel

sealed interface ProfileEvent {
    object LogoutClicked : ProfileEvent
    data class NotificationsToggled(val enabled: Boolean) : ProfileEvent
}
