package com.trobat.data.repository

import android.content.Context
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
        val prefs = context.getSharedPreferences("trobat_prefs", Context.MODE_PRIVATE)
        val sessionManager = SessionManager(prefs)
        NetworkProvider.init(sessionManager)
        val api = NetworkProvider.api

        authRepository = RemoteAuthRepository(api, sessionManager)
        caseRepository = RemoteCaseRepository(api)
        citizenReportRepository = RemoteCitizenReportRepository(api, context.applicationContext)
    }
}
