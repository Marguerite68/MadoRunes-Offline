package com.example.madodict.wiki.data.db

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = WikiItemEntity::class)  // 指向主表
@Entity(tableName = "wiki_items_fts")
data class WikiItemFtsEntity(
    val name: String,
    val content: String,
    val enName: String?
)