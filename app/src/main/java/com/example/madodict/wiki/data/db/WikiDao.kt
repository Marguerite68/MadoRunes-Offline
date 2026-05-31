package com.example.madodict.wiki.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WikiDao {

    // 条目操作

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WikiItemEntity)

    @Update
    suspend fun updateEntry(entry: WikiItemEntity)

    @Query("DELETE FROM wiki_items WHERE entryId = :entryId")
    suspend fun deleteEntry(entryId: String)

    // 名称搜索：匹配name或enName，速度快，作为默认搜索
    @Query("""
    SELECT * FROM wiki_items
    WHERE name LIKE '%' || :keyword || '%'
    OR enName LIKE '%' || :keyword || '%'
    ORDER BY name ASC
""")
    suspend fun searchByName(keyword: String): List<WikiItemEntity>

    // 全文搜索：在名称搜索基础上额外搜索 content
    // 排除已经被名称搜索命中的结果，避免重复
    @Query("""
    SELECT * FROM wiki_items
    WHERE content LIKE '%' || :keyword || '%'
    AND name NOT LIKE '%' || :keyword || '%'
    AND enName NOT LIKE '%' || :keyword || '%'
    ORDER BY name ASC
""")
    suspend fun searchByContent(keyword: String): List<WikiItemEntity>

    // 获取所有条目
    @Query("SELECT * FROM wiki_items ORDER BY name ASC")
    suspend fun getAllEntries(): List<WikiItemEntity>

    // 随机获取一个条目
    @Query("SELECT * FROM wiki_items ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomEntry(): WikiItemEntity?

    // 按 id 获取单个条目（条目详情页使用）
    @Query("SELECT * FROM wiki_items WHERE entryId = :entryId")
    suspend fun getEntryById(entryId: String): WikiItemEntity?

    // 版本记录操作

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersionRecord(record: VersionRecordsEntity)

    @Update
    suspend fun updateVersionRecord(record: VersionRecordsEntity)

    @Query("DELETE FROM version_records WHERE entryId = :entryId")
    suspend fun deleteVersionRecord(entryId: String)

    // 获取所有版本记录（同步时用于找出需要删除的废弃条目）
    @Query("SELECT * FROM version_records")
    suspend fun getAllVersionRecords(): List<VersionRecordsEntity>

    // 按id获取单条版本记录
    @Query("SELECT * FROM version_records WHERE entryId = :entryId")
    suspend fun getVersionRecord(entryId: String): VersionRecordsEntity?

    @Query("INSERT INTO wiki_items_fts(wiki_items_fts) VALUES('rebuild')")
    suspend fun rebuildFts()

}