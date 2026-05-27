package com.trobat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.ui.graphics.vector.ImageVector

data class TrobatBottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenter: Boolean = false,
)

val trobatBottomBarItems = listOf(
    TrobatBottomBarItem(
        route = BottomRoutes.CASES,
        label = "Casos",
        icon = Icons.Outlined.FolderOpen
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.HEATMAP,
        label = "Mapa",
        icon = Icons.Outlined.Map
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.CAMERA,
        label = "Cámara",
        icon = Icons.Outlined.PhotoCamera,
        isCenter = true
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.NOTIFICATIONS,
        label = "Alertas",
        icon = Icons.Outlined.NotificationsNone
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.PROFILE,
        label = "Perfil",
        icon = Icons.Outlined.PersonOutline
    )
)
