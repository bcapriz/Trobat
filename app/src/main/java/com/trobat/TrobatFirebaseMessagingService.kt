package com.trobat

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrobatFirebaseMessagingService : FirebaseMessagingService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["titulo"] ?: return
        val body = message.notification?.body ?: message.data["descripcion"] ?: ""
        val notificationId = message.messageId?.hashCode() ?: title.hashCode()

        CoroutineScope(Dispatchers.IO).launch {
            RepositoryProvider.notificationRepository.save(title, body, notificationId)
        }

        showNotification(title, body, notificationId)
    }

    // FCM rota el token periódicamente; re-subscribimos para no perder el topic
    override fun onNewToken(token: String) {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(TrobatApplication.ALERTS_TOPIC)
            .addOnFailureListener { /* el próximo onNewToken lo reintentará */ }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, body: String, notificationId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, TrobatApplication.ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
