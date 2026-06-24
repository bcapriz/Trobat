package com.trobat.ui.notifications

sealed interface NotificationsEvent {
    object MarkAllRead : NotificationsEvent
}
