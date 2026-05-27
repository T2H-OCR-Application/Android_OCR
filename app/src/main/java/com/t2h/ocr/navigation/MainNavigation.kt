package com.t2h.ocr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.t2h.ocr.ui.screens.file.FileScreen
import com.t2h.ocr.ui.screens.home.HomeScreen
import com.t2h.ocr.ui.screens.profile.ProfileScreen
import com.t2h.ocr.ui.screens.textfile.TextFileScreen
import com.t2h.ocr.ui.screens.tools.ToolsScreen

/**
 * NavHost nội bộ bên trong MainScreen.
 * Quản lý việc chuyển đổi content giữa các tab.
 *
 * @param onSignOut callback từ ProfileScreen khi đăng xuất thành công
 *                  → đẩy lên AppNavigation để navigate về AUTH_GRAPH
 */
@Composable
fun MainNavigation(
    tabNavController : NavHostController,
    onSignOut        : () -> Unit = {},
) {
    // Xử lý chung cho onToolClick — dùng chung cho cả HomeScreen và ToolsScreen
    val handleToolClick: (String) -> Unit = { toolId ->
        when (toolId) {
            "text" -> tabNavController.navigate(NavRoutes.TEXT_FILES)
            // TODO: xử lý các tool khác (pdf, file, scan, image)
        }
    }

    NavHost(
        navController    = tabNavController,
        startDestination = NavRoutes.HOME,
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(onToolClick = handleToolClick)
        }
        composable(NavRoutes.FILE) {
            FileScreen()
        }
        composable(NavRoutes.TOOLS) {
            ToolsScreen(onToolClick = handleToolClick)
        }
        composable(NavRoutes.PROFILE) {
            ProfileScreen(onSignOut = onSignOut)
        }
        composable(NavRoutes.TEXT_FILES) {
            TextFileScreen(onBack = { tabNavController.popBackStack() })
        }
        // NavRoutes.SCAN   — nút Scan launch Camera riêng, không phải tab
        // NavRoutes.SEARCH — Search được xử lý trực tiếp trong MainScreen
    }
}
