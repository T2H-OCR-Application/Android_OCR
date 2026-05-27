package com.t2h.ocr.navigation

object NavRoutes {

    // ── Auth graph ──
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN      = "login"
    const val REGISTER   = "register"

    // ── Main graph ──
    const val MAIN_GRAPH = "main_graph"
    const val MAIN       = "main"        // shell screen

    // ── Bottom tabs (nested nav bên trong MainScreen) ──
    const val HOME    = "home"
    const val FILE    = "file"
    const val SCAN    = "scan"
    const val TOOLS   = "tools"
    const val PROFILE = "profile"

    // ── Sub screens ──
    const val SEARCH     = "search"
    const val TEXT_FILES = "text_files"
}
