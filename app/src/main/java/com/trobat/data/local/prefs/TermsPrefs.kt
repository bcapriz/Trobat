package com.trobat.data.local.prefs

import android.content.Context

class TermsPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasAcceptedTerms: Boolean
        get() = prefs.getBoolean(KEY_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_ACCEPTED, value).apply()

    companion object {
        private const val PREFS_NAME = "trobat_terms_prefs"
        private const val KEY_ACCEPTED = "key_terms_accepted"
    }
}