package com.t2h.ocr.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.t2h.ocr.R
import com.t2h.ocr.navigation.MainNavigation
import com.t2h.ocr.navigation.NavRoutes
import com.t2h.ocr.ui.components.main_components.MainBottomBar
import com.t2h.ocr.ui.components.main_components.MainSearchBar
import com.t2h.ocr.ui.components.main_components.defaultBottomNavItems
import com.t2h.ocr.ui.screens.search.SearchScreen
import com.t2h.ocr.ui.viewmodel.SearchViewModel

/**
 * Shell screen của luồng chính (sau khi đăng nhập).
 *
 * Quản lý trạng thái tìm kiếm:
 *  - isSearching = false → hiển thị tab navigation bình thường
 *  - isSearching = true  → SearchBar active, hiện SearchScreen (không navigate)
 *
 * Layout cố định:
 *  ┌──────────────────────────────┐
 *  │   MainSearchBar              │  ← passive hoặc active tùy isSearching
 *  ├──────────────────────────────┤
 *  │   MainNavigation             │  ← ẩn khi isSearching
 *  │   hoặc SearchScreen          │  ← hiện khi isSearching
 *  ├──────────────────────────────┤
 *  │   MainBottomBar              │  ← ẩn khi isSearching
 *  └──────────────────────────────┘
 */
@Composable
fun MainScreen(
    onSignOut : () -> Unit = {},
) {
    val tabNavController  = rememberNavController()
    val searchViewModel   : SearchViewModel = viewModel()

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route ?: NavRoutes.HOME

    val query         by searchViewModel.query.collectAsStateWithLifecycle()
    val filteredTools by searchViewModel.filteredTools.collectAsStateWithLifecycle()
    val filteredDocs  by searchViewModel.filteredDocs.collectAsStateWithLifecycle()

    var isSearching by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.main_act))
            .statusBarsPadding(),
    ) {
        // ── SearchBar: dùng chung, thay đổi chế độ ──────────────────────
        MainSearchBar(
            isActive      = isSearching,
            query         = query,
            onQueryChange = { searchViewModel.onQueryChange(it) },
            onActivate    = { isSearching = true },
            onDismiss     = {
                isSearching = false
                searchViewModel.onQueryChange("")
            },
            modifier      = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        // ── Content ──────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            if (isSearching) {
                // Hiện kết quả tìm kiếm — không navigate, không tạo SearchBar mới
                SearchScreen(
                    query         = query,
                    filteredTools = filteredTools,
                    filteredDocs  = filteredDocs,
                )
            } else {
                MainNavigation(
                    tabNavController = tabNavController,
                    onSignOut        = onSignOut,
                )
            }
        }

        // ── BottomBar: ẩn khi đang search ───────────────────────────────
        if (!isSearching) {
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
}
