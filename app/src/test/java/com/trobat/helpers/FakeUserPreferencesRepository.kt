package com.trobat.helpers

import com.trobat.data.repository.UserPreferencesRepository

class FakeUserPreferencesRepository(
    private var notificationsEnabled: Boolean = true,
    private var darkModeEnabled: Boolean = false
) : UserPreferencesRepository {

    override fun getNotificationsEnabled(): Boolean = notificationsEnabled
    override fun setNotificationsEnabled(enabled: Boolean) { notificationsEnabled = enabled }
    override fun getDarkModeEnabled(): Boolean = darkModeEnabled
    override fun setDarkModeEnabled(enabled: Boolean) { darkModeEnabled = enabled }
}
