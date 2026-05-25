package com.trobat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.trobat.navigation.AppNavigation
import com.trobat.ui.theme.TrobatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TrobatTheme {
                AppNavigation()
            }
        }
    }
}
