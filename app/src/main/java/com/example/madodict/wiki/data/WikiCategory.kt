package com.example.madodict.wiki.data

// 新增类型只需在这里增加一项；比如LOCATION("03", 3, "地点", 0xFF80CBC4),
// 声明顺序同时决定筛选菜单顺序。"全部"由null表示。
enum class WikiCategory(
    val namespace: String,
    val value: Int,
    val label: String,
    val colorArgb: Long,
    val detailLabel: String = label
) {
    CHARACTER("01", 1, "角色", 0xFFFFB7C5),
    WITCH("00", 2, "魔女", 0xFFD9D9D9),
    ENTRY("02", 0, "词条", 0xFF70838F, "一般词条");

    companion object {
        fun fromNamespace(namespace: String): WikiCategory? =
            entries.firstOrNull { it.namespace == namespace }

        fun fromValue(value: Int): WikiCategory? =
            entries.firstOrNull { it.value == value }
    }
}
