package com.trobat.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.Role
import com.trobat.ui.screen.CoachmarkController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trobat.ui.components.FloatingCameraButton

@Composable
fun TrobatBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCameraClick: (() -> Unit)? = null,
    coachmarkController: CoachmarkController? = null,
    modifier: Modifier = Modifier
) {
    val cameraSelected = currentRoute == BottomRoutes.CAMERA

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .align(Alignment.BottomCenter)
                .border(
                    width = 1.dp,
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                ),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 2.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trobatBottomBarItems.forEach { item ->
                    if (item.isCenter) {
                        Spacer(modifier = Modifier.width(68.dp))
                    } else {
                        TrobatNavigationBarItem(
                            item = item,
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            onBoundsChanged = when (item.route) {
                                BottomRoutes.CASES -> coachmarkController?.let {
                                    { rect -> it.casesBounds.value = rect }
                                }
                                BottomRoutes.HEATMAP -> coachmarkController?.let {
                                    { rect -> it.heatmapBounds.value = rect }
                                }
                                else -> null
                            }
                        )
                    }
                }
            }
        }

        FloatingCameraButton(
            selected = cameraSelected,
            onClick = onCameraClick ?: { onNavigate(BottomRoutes.CAMERA) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 14.dp)
                .then(
                    coachmarkController?.let {
                        Modifier.onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            it.cameraBounds.value = Rect(
                                left = pos.x,
                                top = pos.y,
                                right = pos.x + coords.size.width,
                                bottom = pos.y + coords.size.height
                            )
                        }
                    } ?: Modifier
                )
        )
    }
}

@Composable
private fun RowScope.TrobatNavigationBarItem(
    item: TrobatBottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    onBoundsChanged: ((Rect) -> Unit)? = null
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val itemScale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bottomBarItemScale"
    )

    val itemColor by animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        label = "bottomBarItemColor"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .height(64.dp)
            .scale(itemScale)
            .semantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = item.label
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .then(
                if (onBoundsChanged != null) {
                    Modifier.onGloballyPositioned { coords ->
                        val pos = coords.positionInRoot()
                        onBoundsChanged(
                            Rect(
                                left = pos.x,
                                top = pos.y,
                                right = pos.x + coords.size.width,
                                bottom = pos.y + coords.size.height
                            )
                        )
                    }
                } else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF9F7BFF),
                                    Color(0xFF5E1DD3)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = itemColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = itemColor
        )
    }
}
