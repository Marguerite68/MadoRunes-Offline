package com.example.madodict

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.provider.MediaStore
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.PathOperation

import com.example.madodict.ui.theme.DarkOnPrimaryContainer
import com.example.madodict.ui.theme.DarkSecondary
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
) {
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
                    checked = switchSelected,
                    onCheckedChange = { /* TODO: Handle switch state change */ }
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
