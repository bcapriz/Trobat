package com.trobat.ui.splash


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.trobat.R
import com.trobat.ui.theme.BackgroundPrincipal
import kotlin.math.roundToInt

@Composable
fun LoadingScreen() {
    val loadingText = "Cargando Trobat..."

    val infiniteTransition = rememberInfiniteTransition(label = "loading_text")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = loadingText.length.toFloat() + 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "typing_progress"
    )

    val visibleCharacters = progress
        .roundToInt()
        .coerceIn(0, loadingText.length)

    val displayedText = loadingText.take(visibleCharacters)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp
                )

                Image(
                    painter = painterResource(id = R.drawable.trobatlogo),
                    contentDescription = "Logo de Trobat durante la carga",
                    modifier = Modifier.size(92.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = displayedText,
                style = MaterialTheme.typography.bodyLarge,
                color = BackgroundPrincipal
            )
        }
    }
}