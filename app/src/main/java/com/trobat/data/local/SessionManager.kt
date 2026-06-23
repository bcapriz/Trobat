package com.trobat.data.local

import android.content.SharedPreferences

class SessionManager(private val prefs: SharedPreferences) {

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var nationalId: String?
        get() = prefs.getString(KEY_NATIONAL_ID, null)
        set(value) = prefs.edit().putString(KEY_NATIONAL_ID, value).apply()

    var phone: String?
        get() = prefs.getString(KEY_PHONE, null)
        set(value) = prefs.edit().putString(KEY_PHONE, value).apply()

    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    fun isLoggedIn(): Boolean = token != null

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN = "key_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_NATIONAL_ID = "key_national_id"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_EMAIL = "key_email"
    }
}
