package com.example.madodict

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    data class BottomBarItem(
        val label: String,
        val iconRes: Int,
        val index: Int,
        val iconSize: Dp,
        val padding: Dp
    )

    val items = listOf(
        BottomBarItem("字典", R.drawable.soul_crystal_light_padding, 0, 40.dp, 0.dp),
        BottomBarItem("转换", R.drawable.convert, 1, 30.dp, 0.dp),
        BottomBarItem("设置", R.drawable.setting, 2, 30.dp, 0.dp)
    )

    NavigationBar(
        modifier = Modifier.height(80.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = selectedTab == item.index
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(horizontal = 33.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onTabSelected(item.index) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if(item.index != 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(item.iconSize),
                        tint = contentColor
                    )
                    if (item.index != 0) {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        color = contentColor
                    )
                }
            }
        }
    }
}