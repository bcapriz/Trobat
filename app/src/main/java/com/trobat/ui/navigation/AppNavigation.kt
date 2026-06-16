package com.trobat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trobat.ui.screen.TrobatMainScreen

object AppRoutes {
    const val SPLASH = "splash"
    const val MAIN = "main"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        composable(AppRoutes.SPLASH) {
            SplashRoute(
                onNavigateToHome = {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.MAIN) {
            TrobatMainScreen()
        }
    }
}
