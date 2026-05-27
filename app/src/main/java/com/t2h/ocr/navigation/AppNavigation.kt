package com.t2h.ocr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.t2h.ocr.ui.screens.auth.LoginScreen
import com.t2h.ocr.ui.screens.auth.RegisterScreen
import com.t2h.ocr.ui.screens.main.MainScreen

/**
 * Root navigation graph — entry point duy nhất của toàn bộ app.
 *
 * Chỉ biết 2 graph con:
 *  - AUTH_GRAPH  : Register → Login
 *  - MAIN_GRAPH  : MainScreen (shell chứa tab navigation)
 *
 * Không biết gì về các tab bên trong MainScreen.
 */
@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()

    NavHost(
        navController    = rootNavController,
        startDestination = NavRoutes.AUTH_GRAPH,
    ) {

        // ── Auth graph ────────────────────────────────────────────────────
        navigation(
            route            = NavRoutes.AUTH_GRAPH,
            startDestination = NavRoutes.REGISTER,
        ) {
            composable(NavRoutes.REGISTER) {
                RegisterScreen(
                    onNavigateToLogin = {
                        rootNavController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.REGISTER) { inclusive = true }
                        }
                    },
                    onAuthSuccess = {
                        // Navigate đến MAIN (composable), không phải MAIN_GRAPH (graph route)
                        rootNavController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                )
            }
            composable(NavRoutes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = {
                        rootNavController.navigate(NavRoutes.REGISTER) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onAuthSuccess = {
                        rootNavController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                )
            }
        }

        // ── Main graph ────────────────────────────────────────────────────
        navigation(
            route            = NavRoutes.MAIN_GRAPH,
            startDestination = NavRoutes.MAIN,
        ) {
            composable(NavRoutes.MAIN) {
                MainScreen(
                    onSignOut = {
                        rootNavController.navigate(NavRoutes.AUTH_GRAPH) {
                            popUpTo(NavRoutes.MAIN_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
