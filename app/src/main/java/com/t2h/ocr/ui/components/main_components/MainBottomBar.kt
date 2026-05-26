package com.t2h.ocr.ui.components.main_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R

/**
 * Thanh điều hướng dưới cố định của MainScreen.
 */
@Composable
fun MainBottomBar(
    items         : List<BottomNavItem>,
    currentRoute  : String,
    onTabSelected : (String) -> Unit,
    onScanClick   : () -> Unit = {},
    modifier      : Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, spotColor = Color.Black.copy(alpha = 0.3f))
            .background(colorResource(R.color.bg_color))
            .padding(vertical = 10.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            when {
                item.isCenter -> CenterScanButton(
                    item    = item,
                    onClick = onScanClick,
                )
                else -> RegularTabItem(
                    item       = item,
                    isSelected = item.route == currentRoute,
                    onClick    = { onTabSelected(item.route) },
                )
            }
        }
    }
}

@Composable
private fun CenterScanButton(item: BottomNavItem, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(58.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(colorResource(R.color.Icon_cl))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
    ) {
        if (item.iconRes != null) {
            Icon(
                painter            = painterResource(item.iconRes),
                contentDescription = "Quét",
                tint               = Color.White,
                modifier           = Modifier.size(28.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape),
            )
        }
    }
}

@Composable
private fun RegularTabItem(
    item       : BottomNavItem,
    isSelected : Boolean,
    onClick    : () -> Unit,
) {
    val tintColor = if (isSelected)
        colorResource(R.color.icon_chose)
    else
        colorResource(R.color.Unspecified)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        if (item.iconRes != null) {
            Icon(
                painter            = painterResource(item.iconRes),
                contentDescription = item.label,
                tint               = tintColor,
                modifier           = Modifier.size(24.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(tintColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp)),
            )
        }
        if (item.label.isNotEmpty()) {
            Text(text = item.label, color = tintColor, fontSize = 10.sp)
        }
    }
}
