package com.example.madodict.wiki.data.repository

import com.example.madodict.wiki.data.db.WikiDao
import com.example.madodict.wiki.data.db.WikiItemEntity
import com.example.madodict.wiki.data.json.ExternalLinkJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class WikiItem(
    val entryId: String,
    val category: Int,
    val name: String,
    val imagePath: String?,
    val content: String,
    val externalLinks: List<ExternalLinkJson>,
    val version: Int
)

class WikiRepository(private val dao: WikiDao) {

    private val gson = Gson()
    private val linkListType = object : TypeToken<List<ExternalLinkJson>>() {}.type

    // 搜索条目，支持模糊匹配，返回列表
    suspend fun search(keyword: String): List<WikiItem> =
        dao.searchEntries(keyword).map { it.toModel() }

    // 获取所有条目，返回列表
    suspend fun getAll(): List<WikiItem> =
        dao.getAllEntries().map { it.toModel() }

    // 获取随机条目，返回单个条目
    suspend fun getRandom(): WikiItem? =
        dao.getRandomEntry()?.toModel()

    // 按id获取条目详情，返回单个条目
    suspend fun getById(entryId: String): WikiItem? =
        dao.getEntryById(entryId)?.toModel()

    private fun WikiItemEntity.toModel() = WikiItem(
        entryId = entryId,
        category = category,
        name = name,
        imagePath = imagePath,
        content = content,
        externalLinks = if (externalLinksJson.isNullOrEmpty()) emptyList()
        else gson.fromJson(externalLinksJson, linkListType),
        version = version
    )
}