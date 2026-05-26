package com.trobat.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TrobatMainScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        HeatMapScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )

    }
}