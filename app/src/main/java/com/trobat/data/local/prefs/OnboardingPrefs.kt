package com.trobat.data.local.prefs

import android.content.Context

class OnboardingPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_SEEN, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEN, value).apply()

    var hasSeenCoachmarks: Boolean
        get() = prefs.getBoolean(KEY_COACHMARKS, false)
        set(value) = prefs.edit().putBoolean(KEY_COACHMARKS, value).apply()

    companion object {
        private const val KEY_SEEN = "key_seen"
        private const val KEY_COACHMARKS = "key_coachmarks"
    }
}