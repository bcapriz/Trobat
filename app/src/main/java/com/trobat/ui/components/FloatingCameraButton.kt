package com.trobat.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trobat.R

@Composable
fun FloatingCameraButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraDesc = stringResource(R.string.component_camara_abrir)

    val cameraScale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cameraScale"
    )

    Box(
        modifier = modifier
            .size(66.dp)
            .scale(cameraScale)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = Color(0x225E1DD3),
                spotColor = Color(0x335E1DD3)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55B99CFF),
                        Color(0x227B3DFF),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
            .padding(2.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE8DDFF),
                        Color(0xFFC3ADFF),
                        Color(0xFF8A64F0)
                    )
                ),
                shape = CircleShape
            )
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (selected) {
                        listOf(
                            Color(0xFF9B77FF),
                            Color(0xFF6B3DE3)
                        )
                    } else {
                        listOf(
                            Color(0xFFA98BFF),
                            Color(0xFF7450E8)
                        )
                    }
                )
            )
            .semantics {
                role = Role.Button
                contentDescription = cameraDesc
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}