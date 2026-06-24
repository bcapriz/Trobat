package com.trobat

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.trobat.data.repository.AppContainer
import com.trobat.ui.navigation.AppNavigation
import com.trobat.ui.theme.ThemeManager
import com.trobat.ui.theme.TrobatTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        persistFcmNotificationIfNeeded(intent)
        setContent {
            val darkMode by ThemeManager.darkMode.collectAsState()
            TrobatTheme(darkTheme = darkMode) {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        persistFcmNotificationIfNeeded(intent)
    }

    // Cuando la app está en background/killed, FCM muestra la notificación sin llamar
    // onMessageReceived. Al tocarla, los datos llegan aquí via intent extras.
    private fun persistFcmNotificationIfNeeded(intent: Intent?) {
        if (intent == null) return
        val title = intent.getStringExtra("gcm.notification.title")
            ?: intent.getStringExtra("titulo")
            ?: return
        val body = intent.getStringExtra("gcm.notification.body")
            ?: intent.getStringExtra("descripcion")
            ?: ""
        val id = intent.getStringExtra("google.message_id")?.hashCode()
            ?: (title + body).hashCode()
        lifecycleScope.launch(Dispatchers.IO) {
            AppContainer.notificationRepository.save(title, body, id)
        }
    }
}

