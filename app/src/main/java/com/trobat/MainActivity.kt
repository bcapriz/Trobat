package com.trobat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.trobat.data.repository.RepositoryProvider
import com.trobat.ui.navigation.AppNavigation
import com.trobat.ui.theme.TrobatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryProvider.init(this)
        enableEdgeToEdge()
        setContent {
            TrobatTheme {
                AppNavigation()
            }
        }
    }
}

