package com.example.madodict.wiki.data.sync

import android.util.Log
import com.example.madodict.wiki.data.db.WikiDao
import com.example.madodict.wiki.data.db.VersionRecordsEntity
import com.example.madodict.wiki.data.db.WikiItemEntity
import com.example.madodict.wiki.data.json.EntryJson

import com.example.madodict.wiki.data.json.ItemJsonParser
import com.google.gson.Gson

class ItemSyncManager(
    private val dao: WikiDao,
    private val parser: ItemJsonParser
) {

    private val gson = Gson()

    suspend fun sync() {
        val parsedEntries = parser.parseAll()
        val parsedMap = parsedEntries.associateBy { it.id }

        // 读取数据库中现有的所有版本记录
        val existingRecords = dao.getAllVersionRecords().associateBy { it.entryId }

        // 增/改
        parsedEntries.forEach { json ->
            val existing = existingRecords[json.id]
            when {
                // 版本记录中没有该条目则新增
                existing == null -> {
                    Log.d("EntrySyncManager", "新增条目: ${json.id}")
                    dao.insertEntry(json.toEntity(gson))
                    dao.insertVersionRecord(VersionRecordsEntity(json.id, json.version))
                }
                // 外部条目版本号更大则更新
                json.version > existing.version -> {
                    Log.d("EntrySyncManager", "更新条目: ${json.id}，${existing.version} → ${json.version}")
                    dao.updateEntry(json.toEntity(gson))
                    dao.updateVersionRecord(VersionRecordsEntity(json.id, json.version))
                }
                // 版本相同则跳过
                else -> {
                    Log.d("EntrySyncManager", "跳过条目: ${json.id}，版本无变化")
                }
            }
        }

        // 删
        // 版本记录中存在、但assets中已不存在的条目则删除
        existingRecords.keys
            .filter { it !in parsedMap }
            .forEach { staleId ->
                Log.d("EntrySyncManager", "删除废弃条目: $staleId")
                dao.deleteEntry(staleId)
                dao.deleteVersionRecord(staleId)
            }

        dao.rebuildFts()

        Log.d("EntrySyncManager", "同步完成，共处理 ${parsedEntries.size} 个条目")

        Log.d("EntrySyncManager", "同步完成，共处理 ${parsedEntries.size} 个条目")
    }

    private fun EntryJson.toEntity(gson: Gson) = WikiItemEntity(
        entryId = id,
        category = category,
        name = name,
        imagePath = imagePath,
        content = content,
        externalLinksJson = if (externalLinks.isNullOrEmpty()) null
        else gson.toJson(externalLinks),
        version = version
    )
}