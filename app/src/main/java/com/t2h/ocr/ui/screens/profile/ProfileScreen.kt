package com.t2h.ocr.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.R
import com.t2h.ocr.ui.viewmodel.ProfileViewModel

/**
 * Màn hình Hồ sơ.
 *
 * Layout:
 *  ┌─────────────────────────────────┐
 *  │  [Avatar]  Email                │  ← user info
 *  ├─────────────────────────────────┤
 *  │  [ Đăng xuất ]                  │  ← full-width button
 *  └─────────────────────────────────┘
 *
 * @param onSignOut callback khi đăng xuất thành công → AppNavigation xử lý về Auth
 */
@Composable
fun ProfileScreen(
    onSignOut : () -> Unit = {},
) {
    val viewModel : ProfileViewModel = viewModel()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Khi user = null (vừa sign out) → báo lên để navigate về Auth
    LaunchedEffect(currentUser) {
        if (currentUser == null) onSignOut()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {

        // ── User info ────────────────────────────────────────────────────
        UserInfoSection(
            displayName = currentUser?.displayName,
            email       = currentUser?.email,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Đăng xuất ────────────────────────────────────────────────────
        Button(
            onClick  = { viewModel.signOut() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .height(50.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.Icon_cl),
            ),
        ) {
            Text(
                text       = "Đăng xuất",
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  User info section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UserInfoSection(
    displayName : String?,
    email       : String?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth(),
    ) {
        // Avatar placeholder
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.bg_color)),
        ) {
            Icon(
                painter            = painterResource(R.drawable.profile_bottombar_main),
                contentDescription = "Avatar",
                modifier           = Modifier.size(36.dp),
                tint               = colorResource(R.color.Icon_cl),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!displayName.isNullOrBlank()) {
                Text(
                    text       = displayName,
                    color      = Color.White,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text     = email ?: "Chưa đăng nhập",
                color    = colorResource(R.color.Unspecified),
                fontSize = 13.sp,
            )
        }
    }
}
