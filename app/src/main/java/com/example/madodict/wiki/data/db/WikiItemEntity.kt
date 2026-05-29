package com.example.madodict.wiki.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wiki_items")
data class WikiItemEntity(
    // 与JSON文件中的id字段一致
    @PrimaryKey
    val entryId: String,
    val category: Int,
    val name: String,
    // 图片路径为空时存null
    val imagePath: String?,
    val content: String,
    // 外部链接序列化为 JSON 字符串存储，读取时由Repository负责反序列化
    val externalLinksJson: String?,
    val version: Int
)
