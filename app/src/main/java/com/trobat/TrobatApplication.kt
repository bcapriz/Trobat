package com.trobat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.trobat.data.repository.AppContainer
import com.trobat.ui.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TrobatApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        ThemeManager.init(AppContainer.userPreferencesRepository.getDarkModeEnabled())
        applicationScope.launch {
            AppContainer.citizenReportRepository.resetStuckSending()
            AppContainer.citizenReportRepository.retrySyncPending()
        }
        registerConnectivityCallback()
        createAlertsChannel()
        subscribeToAlertsTopic()
    }

    private fun registerConnectivityCallback() {
        val cm = getSystemService(ConnectivityManager::class.java)
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                applicationScope.launch {
                    AppContainer.citizenReportRepository.retrySyncPending()
                    val location = AppContainer.lastLocationPrefs.load()
                    if (location != null) {
                        AppContainer.caseRepository.refreshCercanosConFallback(location.first, location.second)
                    } else {
                        AppContainer.caseRepository.refresh()
                    }
                }
            }
        })
    }

    private fun createAlertsChannel() {
        val channel = NotificationChannel(
            ALERTS_CHANNEL_ID,
            "Alertas Trobat",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de búsqueda de personas"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun subscribeToAlertsTopic() {
        if (!AppContainer.authRepository.isLoggedIn()) return
        FirebaseMessaging.getInstance()
            .subscribeToTopic(ALERTS_TOPIC)
            .addOnFailureListener { /* se reintentará en el próximo arranque */ }
    }

    companion object {
        const val ALERTS_CHANNEL_ID = "trobat_alertas"
        const val ALERTS_TOPIC = "alertas-trobat"
    }
}
