package com.trobat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trobat.data.repository.RepositoryProvider
import com.trobat.ui.screen.LoginScreen
import com.trobat.ui.screen.RegisterScreen
import com.trobat.ui.screen.TrobatMainScreen

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
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
                onNavigateToMain = {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                },
                isLoggedIn = { RepositoryProvider.authRepository.isLoggedIn() }
            )
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.REGISTER)
                }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.MAIN) {
            TrobatMainScreen()
        }
    }
}
