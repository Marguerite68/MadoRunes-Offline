package com.example.madodict

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madodict.ui.theme.ContrastArchaicText
import com.example.madodict.ui.theme.ContrastGothicText
import com.example.madodict.ui.theme.ContrastHanLatinText
import com.example.madodict.ui.theme.ContrastModrenText
import com.example.madodict.ui.theme.ContrastMusicalText
import com.example.madodict.ui.theme.PageBodyText
import com.example.madodict.ui.theme.PageTitle
import com.example.madodict.ui.theme.MadoDictTheme

data class RuneEntry(
    val latin: String?,
    val ancient: String?,
    val modern: String?,
    val music: String?,
    val gothic: String?
)

private val textStyleByColumnIndex = mapOf(
    1 to ContrastArchaicText,
    2 to ContrastModrenText,
    3 to ContrastMusicalText,
    4 to ContrastGothicText
)


@Composable
fun DictionaryScreen(
    runeEntries: List<RuneEntry>,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    language: AppLanguage = AppLanguage.ZH
) {

    val context = LocalContext.current

    val introText1 = remember(language) { appString(context, language, R.string.dict_intro_para1) }
    val introText2 = remember(language) { appString(context, language, R.string.dict_intro_para2) }
    val introText3 = remember(language) { appString(context, language, R.string.dict_intro_para3) }

    Scaffold(
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                language = language
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 40.dp)
        ) {
            item {
                Text(
                    text = "Dictionary",
                    style = PageTitle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 3.sp
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    text = introText1,   // ← 用缓存值
                    style = PageBodyText,
                    letterSpacing = 1.5.sp
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Text(
                    text = introText2,   // ← 用缓存值
                    style = PageBodyText,
                    letterSpacing = 1.5.sp
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Text(
                    text = introText3,   // ← 用缓存值
                    style = PageBodyText,
                    letterSpacing = 1.5.sp
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            runeTableItems(runeEntries, language)
        }
    }
}

/**
 * Adds the table a row at a time so LazyColumn can compose only the rows near
 * the viewport. Keeping the entire table in one item made every cell compose
 * and measure during tab navigation.
 */
private fun LazyListScope.runeTableItems(
    entries: List<RuneEntry>,
    language: AppLanguage
) {
    val totalRows = entries.size + 1

    item(key = "rune-table-header", contentType = "rune-table-row") {
        RuneTableRowFrame(isFirst = true, isLast = entries.isEmpty()) {
            RuneRow(
                cells = runeHeaderCells(language),
                isHeader = true,
                rowIndex = 0,
                totalRows = totalRows,
                appLanguage = language
            )
        }
    }

    itemsIndexed(
        items = entries,
        key = { index, entry -> "rune-table-${entry.latin ?: "empty"}-$index" },
        contentType = { _, _ -> "rune-table-row" }
    ) { rowIndex, entry ->
        RuneTableRowFrame(isFirst = false, isLast = rowIndex == entries.lastIndex) {
            RuneRow(
                cells = entry.cells(),
                isHeader = false,
                rowIndex = rowIndex + 1,
                totalRows = totalRows,
                appLanguage = language
            )
        }
    }
}

@Composable
private fun runeHeaderCells(language: AppLanguage): List<String?> {
    val context = LocalContext.current
    return remember(language) {
        listOf(
            appString(context, language, R.string.column_and_row_name),
            appString(context, language, R.string.ancient_font_name),
            appString(context, language, R.string.modern_font_name),
            appString(context, language, R.string.musical_font_name),
            appString(context, language, R.string.gothic_font_name)
        )
    }
}

@Composable
private fun RuneTableRowFrame(
    isFirst: Boolean,
    isLast: Boolean,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 36.dp else 0.dp,
        topEnd = if (isFirst) 36.dp else 0.dp,
        bottomStart = if (isLast) 36.dp else 0.dp,
        bottomEnd = if (isLast) 36.dp else 0.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.onPrimary)
            .tableFrameOutline(
                isFirst = isFirst,
                isLast = isLast,
                color = MaterialTheme.colorScheme.outline
            )
            .padding(
                start = 18.dp,
                top = if (isFirst) 18.dp else 0.dp,
                end = 18.dp,
                bottom = if (isLast) 18.dp else 2.dp
            )
    ) {
        content()
    }
}

private fun Modifier.tableFrameOutline(
    isFirst: Boolean,
    isLast: Boolean,
    color: Color
): Modifier = drawBehind {
    val strokeWidth = 3.5.dp.toPx()
    val halfStroke = strokeWidth / 2
    val cornerRadius = 35.dp.toPx()
    val top = if (isFirst) halfStroke else 0f
    val bottom = if (isLast) size.height - halfStroke else size.height

    drawLine(
        color = color,
        start = Offset(halfStroke, top),
        end = Offset(halfStroke, bottom),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(size.width - halfStroke, top),
        end = Offset(size.width - halfStroke, bottom),
        strokeWidth = strokeWidth
    )
    if (isFirst) {
        drawLine(
            color = color,
            start = Offset(cornerRadius, halfStroke),
            end = Offset(size.width - cornerRadius, halfStroke),
            strokeWidth = strokeWidth
        )
    }
    if (isLast) {
        drawLine(
            color = color,
            start = Offset(cornerRadius, size.height - halfStroke),
            end = Offset(size.width - cornerRadius, size.height - halfStroke),
            strokeWidth = strokeWidth
        )
    }
}

private fun RuneEntry.cells(): List<String?> = listOf(latin, ancient, modern, music, gothic)

private val columnWeights = listOf(0.205f, 0.205f, 0.205f, 0.205f, 0.205f)

@Composable
fun RuneRow(
    cells: List<String?>,
    isHeader: Boolean,
    rowIndex: Int,
    totalRows: Int,
    appLanguage: AppLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        cells.forEachIndexed { columnIndex, text ->
            RuneCell(
                text = text,
                weight = columnWeights[columnIndex],
                isLatinColumn = columnIndex == 0,
                isHeader = isHeader,
                columnIndex = columnIndex,
                rowIndex = rowIndex,
                totalRows = totalRows,
                totalColumns = cells.size,
                appLanguage = appLanguage
            )
        }
    }
}

@Composable
fun RowScope.RuneCell(
    text: String?,
    weight: Float,
    isLatinColumn: Boolean,
    isHeader: Boolean,
    columnIndex: Int,
    rowIndex: Int,
    totalRows: Int,
    totalColumns: Int,
    appLanguage: AppLanguage
) {
    val usesStressBackground = isHeader || isLatinColumn

    val bgColor = if (usesStressBackground) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onPrimary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    val cornerRadius = 20.dp

    val shape = remember(rowIndex, columnIndex, totalRows, totalColumns) {
        RoundedCornerShape(
            topStart = if (rowIndex == 0 && columnIndex == 0) cornerRadius else 0.dp,
            topEnd = if (rowIndex == 0 && columnIndex == totalColumns - 1) cornerRadius else 0.dp,
            bottomStart = if (rowIndex == totalRows - 1 && columnIndex == 0) cornerRadius else 0.dp,
            bottomEnd = if (rowIndex == totalRows - 1 && columnIndex == totalColumns - 1) cornerRadius else 0.dp
        )
    }

    val textStyle = remember(text, isHeader, isLatinColumn, columnIndex, appLanguage) {
        val baseStyle = when {
            text == null -> ContrastHanLatinText
            isHeader -> ContrastHanLatinText
            isLatinColumn -> ContrastHanLatinText
            else -> textStyleByColumnIndex[columnIndex] ?: ContrastArchaicText
        }
        when {
            appLanguage != AppLanguage.ZH && isHeader -> baseStyle.copy(fontSize = 9.sp)
            else -> baseStyle
        }
    }

    val displayText = text ?: "/"

    Box(
        modifier = Modifier
            .weight(weight)
            .aspectRatio(1f)
            .border(
                width = 2.5.dp,
                color = outlineVariantColor,
                shape = shape
            )
            .clip(shape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            textAlign = TextAlign.Center,
            style = textStyle,
            modifier = Modifier.padding(2.dp)
        )
    }
}

@Composable
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun DictionaryScreenPreview() {
    MadoDictTheme(darkTheme = false, dynamicColor = false) {
        DictionaryScreen(runeEntries = defaultRuneEntries)
    }
}

@Composable
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun DictionaryScreenDarkPreview() {
    MadoDictTheme(darkTheme = true, dynamicColor = false) {
        DictionaryScreen(runeEntries = defaultRuneEntries)
    }
}

val defaultRuneEntries = listOf(
    RuneEntry("A", "A", "A", "A", "A"),
    RuneEntry("B", "B", "B", "B", "B"),
    RuneEntry("C", "C", "C", "C", "C"),
    RuneEntry("D", "D", "D", "D", "D"),
    RuneEntry("E", "E", "E", "E", "E"),
    RuneEntry("F", "F", "F", "F", "F"),
    RuneEntry("G", "G", "G", "G", "G"),
    RuneEntry("H", "H", "H", "H", "H"),
    RuneEntry("I", "I", "I", "I", "I"),
    RuneEntry("J", "J", "J", null, "J"),
    RuneEntry("K", "K", "K", "K", "K"),
    RuneEntry("L", "L", "L", "L", "L"),
    RuneEntry("M", "M", "M", "M", "M"),
    RuneEntry("N", "N", "N", "N", "N"),
    RuneEntry("O", "O", "O", "O", "O"),
    RuneEntry("P", "P", "P", "P", "P"),
    RuneEntry("Q", "Q", "Q", null, "Q"),
    RuneEntry("R", "R", "R", "R", "R"),
    RuneEntry("S", "S", "S", "S", "S"),
    RuneEntry("T", "T", "T", "T", "T"),
    RuneEntry("U", "U", "U", "U", "U"),
    RuneEntry("V", "V", null, "V", "V"),
    RuneEntry("W", "W", "W", null, "W"),
    RuneEntry("X", "X", null, null, "X"),
    RuneEntry("Y", "Y", "Y", "Y", "Y"),
    RuneEntry("Z", "Z", "Z", "Z", "Z"),
    RuneEntry("Ä", "Ä", "Ä", null, "Ä"),
    RuneEntry("Ö", "Ö", "Ö", null, "Ö"),
    RuneEntry("Ü", "Ü", "Ü", null, "Ü"),
    RuneEntry("ß", "ß", "ß", null, "ß"),
    RuneEntry("0", "0", "0", null, "0"),
    RuneEntry("1", "1", "1", null, "1"),
    RuneEntry("2", "2", "2", null, "2"),
    RuneEntry("3", "3", "3", null, "3"),
    RuneEntry("4", "4", "4", null, "4"),
    RuneEntry("5", "5", "5", null, "5"),
    RuneEntry("6", "6", "6", null, "6"),
    RuneEntry("7", "7", "7", null, "7"),
    RuneEntry("8", "8", "8", null, "8"),
    RuneEntry("9", "9", "9", null, "9")
)
