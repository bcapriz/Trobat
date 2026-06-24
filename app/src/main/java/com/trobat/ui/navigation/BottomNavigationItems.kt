package com.trobat.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.ui.graphics.vector.ImageVector
import com.trobat.R

data class TrobatBottomBarItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val isCenter: Boolean = false,
)

val trobatBottomBarItems = listOf(
    TrobatBottomBarItem(
        route = BottomRoutes.CASES,
        labelRes = R.string.nav_cases,
        icon = Icons.Outlined.FolderOpen
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.HEATMAP,
        labelRes = R.string.nav_map,
        icon = Icons.Outlined.Map
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.CAMERA,
        labelRes = R.string.nav_camera,
        icon = Icons.Outlined.PhotoCamera,
        isCenter = true
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.NOTIFICATIONS,
        labelRes = R.string.nav_alerts,
        icon = Icons.Outlined.NotificationsNone
    ),
    TrobatBottomBarItem(
        route = BottomRoutes.PROFILE,
        labelRes = R.string.nav_profile,
        icon = Icons.Outlined.PersonOutline
    )
)
