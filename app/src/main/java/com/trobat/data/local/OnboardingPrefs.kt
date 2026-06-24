package com.trobat.data.local

import android.content.Context

class OnboardingPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_SEEN, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEN, value).apply()

    companion object {
        private const val KEY_SEEN = "key_seen"
    }
}
