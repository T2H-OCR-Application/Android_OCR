package com.t2h.ocr.ui.components.main_components

import com.t2h.ocr.R
import com.t2h.ocr.navigation.NavRoutes

/**
 * Data model cho một item trong BottomBar.
 */
data class BottomNavItem(
    val route    : String,
    val label    : String,
    val iconRes  : Int?,
    val isCenter : Boolean = false,
    val isAction : Boolean = false,
)

val defaultBottomNavItems = listOf(
    BottomNavItem(
        route   = NavRoutes.HOME,
        label   = "Trang chủ",
        iconRes = R.drawable.home_bottombar_main,
    ),
    BottomNavItem(
        route   = NavRoutes.FILE,
        label   = "Tệp",
        iconRes = R.drawable.file_bottombar_main,
    ),
    BottomNavItem(
        route    = NavRoutes.SCAN,
        label    = "",
        iconRes  = R.drawable.streamline_scanner_solid,
        isCenter = true,
        isAction = true,
    ),
    BottomNavItem(
        route   = NavRoutes.TOOLS,
        label   = "Công cụ",
        iconRes = R.drawable.tool_bottombar_main,
    ),
    BottomNavItem(
        route   = NavRoutes.PROFILE,
        label   = "Hồ sơ",
        iconRes = R.drawable.profile_bottombar_main,
    ),
)
