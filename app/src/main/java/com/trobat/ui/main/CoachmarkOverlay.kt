package com.trobat.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trobat.R
import com.trobat.ui.theme.TrobatOutline
import com.trobat.ui.theme.TrobatPurple
import com.trobat.ui.theme.TrobatSurface
import com.trobat.ui.theme.TrobatText
import com.trobat.ui.theme.TrobatTextSecondary

@Composable
fun CoachmarkOverlay(
    controller: CoachmarkController,
    onDismiss: () -> Unit
) {
    val step = controller.currentStep
    if (step == CoachmarkStep.DONE) return

    val bounds = controller.currentBounds
    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "coachmark_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightPx = with(density) { maxHeight.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(Color.Black.copy(alpha = 0.65f))
            bounds?.let { rect ->
                val center = rect.center
                val baseRadius = maxOf(rect.width, rect.height) / 2f + with(density) { 24.dp.toPx() }
                drawCircle(
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear,
                    radius = baseRadius,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = pulseAlpha),
                    style = Stroke(width = with(density) { 2.dp.toPx() }),
                    radius = baseRadius + with(density) { 8.dp.toPx() },
                    center = center
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        val tooltipBottomDp = with(density) {
            if (bounds != null) {
                (screenHeightPx - bounds.top + 16.dp.toPx()).toDp()
            } else {
                maxHeight * 0.4f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, bottom = tooltipBottomDp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrobatSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(CoachmarkStep.CASES, CoachmarkStep.CAMERA, CoachmarkStep.HEATMAP).forEach { s ->
                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .width(if (s == step) 20.dp else 8.dp)
                                    .background(
                                        color = if (s.ordinal <= step.ordinal) TrobatPurple else TrobatOutline,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(step.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrobatPurple,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(step.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrobatText
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.action_skip), color = TrobatTextSecondary)
                        }
                        Button(
                            onClick = { if (step.isLast) onDismiss() else controller.advance() },
                            colors = ButtonDefaults.buttonColors(containerColor = TrobatPurple)
                        ) {
                            Text(stringResource(if (step.isLast) R.string.action_ok else R.string.action_next))
                        }
                    }
                }
            }
        }
    }
}
