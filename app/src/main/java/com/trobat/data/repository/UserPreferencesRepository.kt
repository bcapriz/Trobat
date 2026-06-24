package com.trobat.data.repository

interface UserPreferencesRepository {
    fun getNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(enabled: Boolean)
    fun getDarkModeEnabled(): Boolean
    fun setDarkModeEnabled(enabled: Boolean)
}
