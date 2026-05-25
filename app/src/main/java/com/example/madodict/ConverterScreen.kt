package com.example.madodict
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.madodict.ui.theme.ContrastArchaicText
import com.example.madodict.ui.theme.ContrastGothicText
import com.example.madodict.ui.theme.ContrastModrenText
import com.example.madodict.ui.theme.ContrastMusicalText
import com.example.madodict.ui.theme.InfoAndBottomBarLabelText
import com.example.madodict.ui.theme.PageBodyText
import com.example.madodict.ui.theme.PageTitle
import com.example.madodict.ui.theme.SettingLabelText
import java.io.File
import java.io.FileOutputStream


@Composable
fun ConverterScreen(
    selectedTab: Int = 1,
    onTabSelected: (Int) -> Unit = {},
    language: AppLanguage = AppLanguage.ZH
) {
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // ── 设置状态 ──────────────────────────────────────────────────────────────
    var selectedFont by remember { mutableStateOf(WitchFontType.ANCIENT) }
    var fontMenuExpanded by remember { mutableStateOf(false) }

    var selectedSize by remember { mutableStateOf(15) }
    var sizeMenuExpanded by remember { mutableStateOf(false) }

    var colorHex by remember { mutableStateOf("6E6488") }
    val textColor = remember(colorHex) {
        runCatching {
            val c = colorHex.trimStart('#')
            if (c.length == 6) Color(("FF$c").toLong(16).toInt()) else null
        }.getOrNull() ?: Color(0xFF6E6488.toInt())
    }

    val fontSizes = listOf(12, 14, 15, 18, 20, 24, 28, 32)
    val controlWidth = 90.dp
    val controlHeight = 25.dp

    val previewTextStyle = when (selectedFont) {
        WitchFontType.ANCIENT -> ContrastArchaicText
        WitchFontType.MODERN  -> ContrastModrenText
        WitchFontType.MUSICAL -> ContrastMusicalText
        WitchFontType.GOTHIC  -> ContrastGothicText
    }

    val previewText = remember(inputText) {
        inputText
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                language = language
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 30.dp, vertical = 40.dp)
        ) {
            // ── 标题 ──────────────────────────────────────────────────────────
            Text(
                text = "Converter",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = PageTitle,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = appString(context, language, R.string.converter_intro),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = PageBodyText
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── 输入框 ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp), clip = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = appString(context, language, R.string.converter_input_hint),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        style = PageBodyText
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = PageBodyText.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier.fillMaxSize(),
                    decorationBox = { it() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // ── 字体选择 ──────────────────────────────────────────────────────
            ConverterSettingRow(
                label = appString(context, language, R.string.font_selection_dialog_title)
            ) {
                Box {
                    ConverterDropdownButton(
                        text = when (selectedFont) {
                            WitchFontType.ANCIENT -> appString(context, language, R.string.ancient_font_name)
                            WitchFontType.MODERN  -> appString(context, language, R.string.modern_font_name)
                            WitchFontType.MUSICAL -> appString(context, language, R.string.musical_font_name)
                            WitchFontType.GOTHIC  -> appString(context, language, R.string.gothic_font_name)
                        },
                        onClick = { fontMenuExpanded = true },
                        modifier = Modifier
                            .width(controlWidth)
                            .height(controlHeight),
                    )
                    DropdownMenu(
                        expanded = fontMenuExpanded,
                        onDismissRequest = { fontMenuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        WitchFontType.entries.forEach { font ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (font) {
                                            WitchFontType.ANCIENT -> appString(context, language, R.string.ancient_font_name)
                                            WitchFontType.MODERN  -> appString(context, language, R.string.modern_font_name)
                                            WitchFontType.MUSICAL -> appString(context, language, R.string.musical_font_name)
                                            WitchFontType.GOTHIC  -> appString(context, language, R.string.gothic_font_name)
                                        },
                                        style = PageBodyText,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedFont = font
                                    fontMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 字号选择 ──────────────────────────────────────────────────────
            ConverterSettingRow(
                label = appString(context, language, R.string.text_size_selection_dialog_title)
            ) {
                Box {
                    ConverterDropdownButton(
                        text = selectedSize.toString(),
                        onClick = { sizeMenuExpanded = true },
                        modifier = Modifier
                            .width(controlWidth)
                            .height(controlHeight)
                    )
                    DropdownMenu(
                        expanded = sizeMenuExpanded,
                        onDismissRequest = { sizeMenuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        fontSizes.forEach { size ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = size.toString(),
                                        style = PageBodyText,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedSize = size
                                    sizeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 颜色选择 ──────────────────────────────────────────────────────
            ConverterSettingRow(
                label = appString(context, language, R.string.text_color)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(textColor)
                    )
                    BasicTextField(
                        value = "#$colorHex",
                        onValueChange = { v ->
                            colorHex = v.trimStart('#').take(6).uppercase()
                        },
                        textStyle = PageBodyText.copy(fontSize = 11.sp),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) { innerTextField() }
                        },
                        modifier = Modifier.width(controlWidth).height(controlHeight)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 2.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── 实时预览 ──────────────────────────────────────────────────────
            Text(
                text = appString(context, language, R.string.real_time_preview),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = PageBodyText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp), clip = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                if (previewText.isEmpty()) {
                    Text(
                        text = appString(context, language, R.string.output_box_hint),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        style = PageBodyText
                    )
                } else {
                    Text(
                        text = previewText,
                        style = previewTextStyle.copy(
                            fontSize = selectedSize.sp,
                            color = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 导出按钮 ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (previewText.isNotEmpty()) {
                            exportAsPng(context, previewText, selectedSize, textColor)
                        } else {
                            Toast.makeText(
                                context,
                                appString(context, language, R.string.converter_export_empty_hint),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .width(110.dp)
                        .height(30.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = appString(context, language, R.string.png_export_button),
                        style = InfoAndBottomBarLabelText,
                    )
                }

                Button(
                    onClick = {
                        if (previewText.isNotEmpty()) {
                            exportAsSvg(context, previewText, selectedSize, textColor)
                        } else {
                            Toast.makeText(
                                context,
                                appString(context, language, R.string.converter_export_empty_hint),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .width(110.dp)
                        .height(30.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = appString(context, language, R.string.svg_export_button),
                        style = InfoAndBottomBarLabelText
                    )
                }
            }
        }
    }
}

// ── 辅助组件 ──────────────────────────────────────────────────────────────────

@Composable
private fun ConverterSettingRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = SettingLabelText
        )
        content()
    }
}

@Composable
private fun ConverterDropdownButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = PageBodyText,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            painter = painterResource(id = R.drawable.down_arrow),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(12.dp),
        )
    }
}

// ── 字体枚举 ──────────────────────────────────────────────────────────────────

enum class WitchFontType { ANCIENT, MODERN, MUSICAL, GOTHIC }


// ── 导出工具 ──────────────────────────────────────────────────────────────────

private fun exportAsPng(
    context: Context,
    text: String,
    sizeSp: Int,
    color: Color
) {
    try {
        val density = context.resources.displayMetrics.scaledDensity
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizeSp * density
            this.color = color.toArgb()
        }
        val padding = 48f
        val lines = text.split("\n")
        val lineH = paint.fontSpacing
        val w = ((lines.maxOfOrNull { paint.measureText(it) } ?: 200f) + padding * 2).coerceAtLeast(200f)
        val h = (lineH * lines.size + padding * 2).coerceAtLeast(100f)

        val bmp = android.graphics.Bitmap.createBitmap(w.toInt(), h.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.WHITE)
        lines.forEachIndexed { i, line ->
            canvas.drawText(line, padding, padding + lineH * (i + 1), paint)
        }

        val file = File(context.cacheDir, "witch_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        shareFile(context, file, "image/png")
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
    }
}

private fun exportAsSvg(context: Context, text: String, sizeSp: Int, color: Color) {
    try {
        val hex = "#%06X".format(color.toArgb() and 0xFFFFFF)
        val lines = text.split("\n")
        val lineH = sizeSp * 1.6f
        val svgH = lineH * lines.size + 40f
        val linesXml = lines.mapIndexed { i, line ->
            """  <text x="20" y="${(20 + lineH * (i + 1)).toInt()}" font-size="$sizeSp" fill="$hex" font-family="serif">$line</text>"""
        }.joinToString("\n")
        val svg = """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="600" height="${svgH.toInt()}">
  <rect width="600" height="${svgH.toInt()}" fill="white"/>
$linesXml
</svg>"""
        val file = File(context.cacheDir, "witch_${System.currentTimeMillis()}.svg")
        file.writeText(svg)
        shareFile(context, file, "image/svg+xml")
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
    }
}

private fun shareFile(context: Context, file: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

@Preview
@Composable
fun ConverterScreenPreview() {
    ConverterScreen()
}