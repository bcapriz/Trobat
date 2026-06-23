package com.trobat.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.trobat.TrobatApplication
import com.trobat.data.local.LastLocationPrefs
import com.trobat.data.local.SessionManager
import com.trobat.data.local.TrobatDatabase
import com.trobat.data.remote.NetworkProvider
import com.trobat.ui.theme.ThemeManager
import java.io.File

object RepositoryProvider {

    lateinit var authRepository: AuthRepository
        private set

    lateinit var caseRepository: CaseRepository
        private set

    lateinit var citizenReportRepository: CitizenReportRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    lateinit var lastLocationPrefs: LastLocationPrefs
        private set

    private fun createEncryptedPrefs(context: Context): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return try {
            EncryptedSharedPreferences.create(
                context,
                "trobat_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            // Keystore key mismatch (e.g. reinstall on device with stale prefs file).
            // Delete the corrupted file and recreate — user will need to log in again.
            File(context.filesDir.parent, "shared_prefs/trobat_secure_prefs.xml").delete()
            EncryptedSharedPreferences.create(
                context,
                "trobat_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun init(context: Context) {
        val prefs = createEncryptedPrefs(context)
        val sessionManager = SessionManager(prefs)
        NetworkProvider.init(sessionManager)
        val api = NetworkProvider.api
        val appScope = (context.applicationContext as TrobatApplication).applicationScope
        val db = TrobatDatabase.build(context.applicationContext)

        lastLocationPrefs = LastLocationPrefs(context.applicationContext)
        authRepository = RemoteAuthRepository(api, sessionManager, db, lastLocationPrefs, context.applicationContext)
        ThemeManager.init(sessionManager.darkModeEnabled)
        caseRepository = RemoteCaseRepository(api, appScope, db)
        citizenReportRepository = RemoteCitizenReportRepository(api, context.applicationContext, db.pendingReportDao(), authRepository)
        notificationRepository = NotificationRepository(db.notificationDao())
    }
}
