package com.trobat.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.trobat.TrobatApplication
import com.trobat.data.local.SessionManager
import com.trobat.data.remote.NetworkProvider

object RepositoryProvider {

    lateinit var authRepository: AuthRepository
        private set

    lateinit var caseRepository: CaseRepository
        private set

    lateinit var citizenReportRepository: CitizenReportRepository
        private set

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            "trobat_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val sessionManager = SessionManager(prefs)
        NetworkProvider.init(sessionManager)
        val api = NetworkProvider.api
        val appScope = (context.applicationContext as TrobatApplication).applicationScope

        authRepository = RemoteAuthRepository(api, sessionManager)
        caseRepository = RemoteCaseRepository(api, appScope)
        citizenReportRepository = RemoteCitizenReportRepository(api, context.applicationContext)
    }
}
