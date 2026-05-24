package com.example.madodict

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.madodict.ui.theme.DarkOnPrimaryContainer
import com.example.madodict.ui.theme.DarkSecondary
import com.example.madodict.ui.theme.InfoAndBottomBarLabelText
import com.example.madodict.ui.theme.LanguageNameText
import com.example.madodict.ui.theme.LightBackground
import com.example.madodict.ui.theme.LightSecondary
import com.example.madodict.ui.theme.MadoDictTheme
import com.example.madodict.ui.theme.PageTitle
import com.example.madodict.ui.theme.SettingLabelText

@Composable
fun SettingsScreen(
    selectedTab: Int = 1,
    onTabSelected: (Int) -> Unit = {},
    switchSelected: Boolean,
    onThemeChange: (Boolean) -> Unit = {}
) {
    var isDarkTheme by remember { mutableStateOf(switchSelected) }
    val radioOptions = listOf("中文", "English", "日本語")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    LaunchedEffect(switchSelected) {
        isDarkTheme = switchSelected
    }

    val uriHandler = LocalUriHandler.current

    fun openUri(url: String) {
        uriHandler.openUri(url)
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 30.dp, vertical = 50.dp)
        ) {
            Text(
                text = "Settings",
                style = PageTitle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dark_mode),
                    contentDescription = "Dark Mode Icon",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "深色模式",
                    style = SettingLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                FixedThumbSwitch(
                    checked = isDarkTheme,
                    onCheckedChange = { newValue ->
                        isDarkTheme = newValue
                        onThemeChange(newValue)
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .height(57.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.language_setting_icon),
                    contentDescription = "Language Setting Icon",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "语言/Language",
                    style = SettingLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth()
                    .height(57.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                radioOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .then(
                                if (option != "English" && option != "Japanese" && option != "日本語") {
                                    Modifier.clickable { onOptionSelected(option) }
                                } else {
                                    Modifier // 不添加 clickable，变为不可点击
                                }
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (option != "English" && option != "Japanese" && option != "日本語") {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape
                                )
                        ) {
                            // 选中时显示中间的小圆点
                            if (selectedOption == option) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimaryContainer)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = option,
                            style = LanguageNameText,
                            color = if (option != "English" && option != "Japanese" && option != "日本語") {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            },
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                thickness = 2.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.Info),
                    contentDescription = "Info Icon",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )
                Spacer(modifier = Modifier.width(13.dp))
                Text(
                    text = "关于",
                    style = SettingLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .padding(start = 84.dp, end = 20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "版本: v 0.0.1a",
                    style = InfoAndBottomBarLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "创作者: Marguerite68",
                    style = InfoAndBottomBarLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )

                // Github 仓库 - 超链接
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "仓库: ",
                        style = InfoAndBottomBarLabelText,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Github",
                        style = InfoAndBottomBarLabelText.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable {
                            openUri("https://github.com/Marguerite68/Mado_Dict")
                        }
                    )
                }
                Text(
                    text = "遵循CC BY-NC 4.0协议，严禁商用",
                    style = InfoAndBottomBarLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "友情链接/参考：",
                    style = InfoAndBottomBarLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )
                // 魔女文网站
                Text(
                    text = "魔女文网站",
                    style = InfoAndBottomBarLabelText.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        openUri("https://www.madorunes.cn")
                    }
                )

                // 萌娘百科
                Text(
                    text = "萌娘百科",
                    style = InfoAndBottomBarLabelText.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        openUri("https://zh.moegirl.org.cn/%E9%AD%94%E5%A5%B3%E6%96%87%E5%AD%97")
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "项目贡献者：",
                    style = InfoAndBottomBarLabelText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun FixedThumbSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    thumbSize: Dp = 24.dp,
    trackWidth: Dp = 59.dp,
    trackHeight: Dp = 28.dp,
    checkedColor: Color = DarkSecondary,
    uncheckedColor: Color = LightSecondary,
    checkedThumbColor: Color = DarkOnPrimaryContainer,
    uncheckedThumbColor: Color = LightBackground
) {
    val trackPadding = 3.dp
    val travelDistance = trackWidth - (trackPadding * 2) - thumbSize
    val shadowColor = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        Color.Black.copy(alpha = 0.1f)
    }

    val offset by animateDpAsState(
        targetValue = if (checked) travelDistance else 0.dp,
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(percent = 50))
                .innerBorderShadow(
                    color = shadowColor,
                    borderWidth = 2.dp,
                    blur = if(checked) 4.dp else 3.dp,
                    cornerRadius = trackHeight / 2
                )
                .background(if (checked) checkedColor else uncheckedColor)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(trackPadding)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = offset)
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(if (checked) checkedThumbColor else uncheckedThumbColor)
            )
        }
    }
}

private fun Modifier.innerBorderShadow(
    color: Color,
    borderWidth: Dp,
    blur: Dp,
    cornerRadius: Dp
): Modifier = drawWithCache {
    val borderPx = borderWidth.toPx()
    val blurPx = blur.toPx()
    val radiusPx = cornerRadius.toPx()
    val rect = Rect(0f, 0f, size.width, size.height)
    val outer = Path().apply { addRoundRect(RoundRect(rect, radiusPx, radiusPx)) }
    val inner = Path().apply { addRoundRect(RoundRect(rect.inflate(-borderPx), radiusPx - borderPx, radiusPx - borderPx)) }
    val borderPath = Path().apply {
        op(outer, inner, PathOperation.Difference)
    }

    onDrawWithContent {
        drawContent()
        drawIntoCanvas { canvas ->
            val paint = Paint().apply { this.color = Color.Transparent }
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.isAntiAlias = true
            frameworkPaint.setShadowLayer(blurPx, 0f, 0f, color.toArgb())
            canvas.drawPath(borderPath, paint)
        }
    }
}

@Composable
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun SettingsScreenLightPreview() {
    MadoDictTheme(darkTheme = false, dynamicColor = false) {
        SettingsScreen(switchSelected = false)
    }
}

@Composable
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun SettingsScreenDarkPreview() {
    MadoDictTheme(darkTheme = true, dynamicColor = false) {
        SettingsScreen(switchSelected = true)
    }
}
