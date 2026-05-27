package com.t2h.ocr.ui.components.main_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R

/**
 * Thanh tìm kiếm của MainScreen.
 *
 * Có 2 chế độ:
 *  - isActive = false → placeholder, bấm vào thì gọi onActivate
 *  - isActive = true  → ô nhập thật, hiện nút "Hủy"
 *
 * @param isActive      true khi đang ở chế độ search
 * @param query         giá trị đang nhập
 * @param onQueryChange callback khi query thay đổi
 * @param onActivate    callback khi bấm vào lúc passive
 * @param onDismiss     callback khi bấm "Hủy"
 */
@Composable
fun MainSearchBar(
    isActive      : Boolean = false,
    query         : String = "",
    onQueryChange : (String) -> Unit = {},
    onActivate    : () -> Unit = {},
    onDismiss     : () -> Unit = {},
    modifier      : Modifier = Modifier,
) {
    val focusRequester     = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Tự focus khi chuyển sang active
    LaunchedEffect(isActive) {
        if (isActive) focusRequester.requestFocus()
    }

    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Search input box ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(colorResource(R.color.bg_color))
                .then(
                    if (!isActive) Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onActivate,
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter            = painterResource(R.drawable.searchbar_main),
                contentDescription = "Tìm kiếm",
                tint               = colorResource(R.color.Unspecified),
                modifier           = Modifier.size(18.dp),
            )

            Spacer(modifier = Modifier.width(10.dp))

            if (isActive) {
                BasicTextField(
                    value           = query,
                    onValueChange   = onQueryChange,
                    modifier        = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine      = true,
                    cursorBrush     = SolidColor(colorResource(R.color.Icon_cl)),
                    textStyle       = TextStyle(color = Color.White, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text     = "Tìm kiếm tài liệu, công cụ...",
                                color    = colorResource(R.color.Unspecified),
                                fontSize = 14.sp,
                            )
                        }
                        innerTextField()
                    },
                )

                // Nút xóa query
                if (query.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter            = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Xóa",
                        tint               = colorResource(R.color.Unspecified),
                        modifier           = Modifier
                            .size(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                            ) { onQueryChange("") },
                    )
                }
            } else {
                Text(
                    text     = "Tìm kiếm tài liệu, ảnh...",
                    color    = colorResource(R.color.Unspecified),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Nút Hủy (chỉ hiện khi active) ───────────────────────────────
        if (isActive) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text     = "Hủy",
                color    = colorResource(R.color.Icon_cl),
                fontSize = 15.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                ) {
                    keyboardController?.hide()
                    onDismiss()
                },
            )
        }
    }
}
