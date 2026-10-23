package com.example.madodict.wiki.data

import androidx.annotation.StringRes
import com.example.madodict.R

// 新增类型时在这里增加一项，并在各语言 strings.xml 中补充标签资源。
// 声明顺序同时决定筛选菜单顺序。"全部"由null表示。
enum class WikiCategory(
    val namespace: String,
    val value: Int,
    @StringRes val labelRes: Int,
    val colorArgb: Long,
    @StringRes val detailLabelRes: Int = labelRes
) {
    CHARACTER("01", 1, R.string.wiki_category_character, 0xFFFFB7C5),
    WITCH("00", 2, R.string.wiki_category_witch, 0xFFD9D9D9),
    ENTRY("02", 0, R.string.wiki_category_entry, 0xFF70838F, R.string.wiki_category_general_entry);

    companion object {
        fun fromNamespace(namespace: String): WikiCategory? =
            entries.firstOrNull { it.namespace == namespace }

        fun fromValue(value: Int): WikiCategory? =
            entries.firstOrNull { it.value == value }
    }
}
