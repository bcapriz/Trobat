package com.trobat.ui.profile

sealed interface ProfileEvent {
    object LogoutClicked : ProfileEvent
    data class NotificationsToggled(val enabled: Boolean) : ProfileEvent
    data class DarkModeToggled(val enabled: Boolean) : ProfileEvent
}
