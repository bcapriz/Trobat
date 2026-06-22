package com.trobat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.messaging.FirebaseMessaging
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TrobatApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        RepositoryProvider.init(this)
        createAlertsChannel()
        subscribeToAlertsTopic()
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
        FirebaseMessaging.getInstance()
            .subscribeToTopic(ALERTS_TOPIC)
            .addOnFailureListener { /* se reintentará en el próximo arranque */ }
    }

    companion object {
        const val ALERTS_CHANNEL_ID = "trobat_alertas"
        const val ALERTS_TOPIC = "alertas-trobat"
    }
}
