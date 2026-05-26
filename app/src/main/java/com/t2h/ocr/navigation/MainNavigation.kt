package com.t2h.ocr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.t2h.ocr.ui.screens.file.FileScreen
import com.t2h.ocr.ui.screens.home.HomeScreen
import com.t2h.ocr.ui.screens.profile.ProfileScreen
import com.t2h.ocr.ui.screens.tools.ToolsScreen

/**
 * NavHost nội bộ bên trong MainScreen.
 * Quản lý việc chuyển đổi content giữa các tab.
 *
 * Lưu ý: NavRoutes.SCAN không có composable ở đây vì nút Scan
 * trên BottomBar sẽ launch Camera trực tiếp (không phải chuyển tab).
 */
@Composable
fun MainNavigation(tabNavController: NavHostController) {
    NavHost(
        navController    = tabNavController,
        startDestination = NavRoutes.HOME,
    ) {
        composable(NavRoutes.HOME)    { HomeScreen() }
        composable(NavRoutes.FILE)    { FileScreen() }
        composable(NavRoutes.TOOLS)   { ToolsScreen() }
        composable(NavRoutes.PROFILE) { ProfileScreen() }
        // NavRoutes.SCAN không có ở đây — nút Scan launch Camera riêng
    }
}
