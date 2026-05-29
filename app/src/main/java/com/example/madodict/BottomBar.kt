package com.example.madodict

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madodict.ui.theme.InfoAndBottomBarLabelText

@Composable
fun BottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    language: AppLanguage = AppLanguage.ZH
) {
    data class BottomBarItem(
        val label: String,
        val iconRes: Int,
        val index: Int,
        val iconSize: Dp,
        val padding: Dp
    )

    val context = LocalContext.current

    val items = listOf(
        BottomBarItem(
            appString(context, language, R.string.bottom_bar_dict),
            R.drawable.soul_crystal_light_padding,
            0,
            40.dp,
            0.dp
        ),
        BottomBarItem(
            appString(context, language, R.string.bottom_bar_converter),
            R.drawable.convert,
            1,
            30.dp,
            0.dp
        ),
        BottomBarItem(
            appString(context, language, R.string.bottom_bar_wiki),
            R.drawable.wiki,
            2,
            30.dp,
            0.dp
        ),
        BottomBarItem(
            appString(context, language, R.string.bottom_bar_settings),
            R.drawable.setting,
            3,
            30.dp,
            0.dp
        )
    )

    NavigationBar(
        modifier = Modifier.height(110.dp),
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
                    .height(65.dp)
                    .padding(horizontal = 12.dp)
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
                        style = InfoAndBottomBarLabelText,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    BottomBar(
        selectedTab = 0,
        onTabSelected = {},
        language = AppLanguage.ZH
    )
}