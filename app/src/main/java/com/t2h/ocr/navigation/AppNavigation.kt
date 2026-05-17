package com.t2h.ocr.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.PaddingValues
import com.t2h.ocr.ui.screens.auth.LoginScreen
import com.t2h.ocr.ui.screens.auth.RegisterScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "register"
    ) {

        composable("register") {

            RegisterScreen(
                paddingValues = PaddingValues(0.dp),
                navController = navController
            )
        }

        composable("login") {

            LoginScreen()

        }
    }
}