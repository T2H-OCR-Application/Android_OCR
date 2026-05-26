package com.t2h.ocr.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.t2h.ocr.R
import com.t2h.ocr.navigation.MainNavigation
import com.t2h.ocr.navigation.NavRoutes
import com.t2h.ocr.ui.components.main_components.MainBottomBar
import com.t2h.ocr.ui.components.main_components.MainSearchBar
import com.t2h.ocr.ui.components.main_components.defaultBottomNavItems

/**
 * Shell screen của luồng chính (sau khi đăng nhập).
 *
 * Layout cố định:
 *  ┌──────────────────────┐
 *  │   MainSearchBar      │  ← không đổi khi chuyển tab
 *  ├──────────────────────┤
 *  │   MainNavigation     │  ← content thay đổi theo tab
 *  ├──────────────────────┤
 *  │   MainBottomBar      │  ← không đổi khi chuyển tab
 *  └──────────────────────┘
 *
 * Nút Scan ở giữa BottomBar KHÔNG chuyển tab — nó sẽ launch Camera
 * thông qua onScanClick (hiện để trống, TODO khi implement CameraX).
 */
@Composable
fun MainScreen() {
    val tabNavController = rememberNavController()

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
        ?: NavRoutes.HOME

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.main_act))
            .statusBarsPadding(),
    ) {
        // ── SearchBar: cố định ───────────────────────────────────────────
        MainSearchBar(
            onSearchClick = { /* TODO: navigate to SearchScreen */ },
            modifier      = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        // ── Content: thay đổi theo tab ───────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            MainNavigation(tabNavController = tabNavController)
        }

        // ── BottomBar: cố định ───────────────────────────────────────────
        MainBottomBar(
            items         = defaultBottomNavItems,
            currentRoute  = currentRoute,
            onTabSelected = { route ->
                tabNavController.navigate(route) {
                    popUpTo(tabNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState    = true
                }
            },
            onScanClick = {
                // TODO: launch Camera / ScanActivity khi implement CameraX
            },
        )
    }
}
