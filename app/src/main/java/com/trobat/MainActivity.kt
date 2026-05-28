package com.trobat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.trobat.ui.navigation.AppNavigation
import com.trobat.ui.theme.TrobatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrobatTheme {
                AppNavigation()
            }
        }
    }
}

