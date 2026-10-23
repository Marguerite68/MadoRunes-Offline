package com.example.madodict.wiki.data.sync

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.madodict.wiki.data.db.VersionRecordsEntity
import com.example.madodict.wiki.data.db.WikiDatabase
import com.example.madodict.wiki.data.db.WikiItemEntity
import com.example.madodict.wiki.data.json.ItemJsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ItemSyncManagerTest {
    private lateinit var database: WikiDatabase
    private val dao get() = database.encyclopediaDao()

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            WikiDatabase::class.java
        ).build()
        dao.insertEntry(WikiItemEntity("010001", 1, "original", null, null, "original", null, 2))
        dao.insertVersionRecord(VersionRecordsEntity("010001", 2))
        dao.rebuildFts()
    }

    @After
    fun tearDown() { database.close() }

    private fun json(id: String, version: Int = 3) =
        """{"id":"$id","category":1,"name":"replacement","content":"replacement","version":$version}"""

    private fun parser(vararg entries: Pair<String, String>) = ItemJsonParser(
        listFiles = { entries.map { it.first }.toTypedArray() },
        readFile = { name -> entries.first { it.first == name }.second }
    )

    private fun validParser() = parser(
        "010001_first.json" to json("010001"),
        "010002_second.json" to json("010002")
    )

    private suspend fun snapshot() = Triple(dao.getAllEntries(), dao.getAllVersionRecords(), ftsIds())

    private fun ftsIds(): List<Long> = database.openHelper.readableDatabase.query(
        "SELECT docid FROM wiki_items_fts WHERE wiki_items_fts MATCH 'original' ORDER BY docid"
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getLong(0)) } }

    private suspend fun expectFailure(block: suspend () -> Unit) {
        var failed = false
        try { block() } catch (_: Exception) { failed = true }
        assertTrue("Expected injected failure", failed)
    }

    @Test
    fun incompleteResourcesNeverMutateEitherOperation() = runBlocking {
        val before = snapshot()
        val invalidParsers = listOf(
            ItemJsonParser({ throw IOException("directory unavailable") }, { "" }),
            ItemJsonParser({ null }, { "" }),
            parser(),
            ItemJsonParser({ arrayOf("010001_first.json") }, { throw IOException("read failed") }),
            parser("010001_first.json" to json("010001"), "010002_second.json" to "{broken"),
            parser("010001_first.json" to "null"),
            parser("010001_first.json" to """{"id":"010001","category":1}"""),
            parser("010001_first.json" to json("020001")),
            parser("010001_first.json" to json("010001"), "010001_duplicate.json" to json("010001"))
        )
        for (resources in invalidParsers) {
            val manager = ItemSyncManager(database, resources)
            expectFailure { manager.sync() }
            assertEquals(before, snapshot())
            expectFailure { manager.reload() }
            assertEquals(before, snapshot())
        }
    }

    @Test
    fun writeFailureRollsBackEntriesVersionsAndFts() = runBlocking {
        val before = snapshot()
        // 第一条已写入、第二条词条也已插入后，强制版本记录写入失败。
        database.openHelper.writableDatabase.execSQL("""
            CREATE TRIGGER fail_version BEFORE INSERT ON version_records
            WHEN NEW.entryId = '010002' BEGIN SELECT RAISE(ABORT, 'injected write failure'); END
        """.trimIndent())
        val manager = ItemSyncManager(database, validParser())
        expectFailure { manager.sync() }
        assertEquals(before, snapshot())
        expectFailure { manager.reload() }
        assertEquals(before, snapshot())
    }

    @Test
    fun cancellationAfterClearOrInsertRollsBackReload() = runBlocking {
        val before = snapshot()
        for (checkpoint in 0..2) {
            var cancelled = false
            try {
                ItemSyncManager(database, validParser()).reload { done, _ ->
                    if (done == checkpoint) throw CancellationException("cancel at $checkpoint")
                }
            } catch (_: CancellationException) { cancelled = true }
            assertTrue(cancelled)
            assertEquals(before, snapshot())
        }
    }

    @Test
    fun unavailableFtsAbortsBothOperations() = runBlocking {
        // 移除索引以模拟索引异常；Room的索引触发器或rebuild抛错均须保留词条和版本。
        database.openHelper.writableDatabase.execSQL("DROP TABLE wiki_items_fts")
        val entries = dao.getAllEntries()
        val versions = dao.getAllVersionRecords()
        val manager = ItemSyncManager(database, validParser())
        expectFailure { manager.sync() }
        assertEquals(entries, dao.getAllEntries())
        assertEquals(versions, dao.getAllVersionRecords())
        expectFailure { manager.reload() }
        assertEquals(entries, dao.getAllEntries())
        assertEquals(versions, dao.getAllVersionRecords())
    }

    @Test
    fun successfulSyncPreservesVersionComparisonDeletesStaleAndRebuildsFts() = runBlocking {
        val manager = ItemSyncManager(database, parser(
            "010001_first.json" to json("010001", 1),
            "010002_second.json" to json("010002")
        ))
        manager.sync()
        assertEquals("original", dao.getEntryById("010001")!!.name)
        assertEquals(2, dao.getVersionRecord("010001")!!.version)
        ItemSyncManager(database, parser("010002_second.json" to json("010002", 4))).sync()
        assertEquals(listOf("010002"), dao.getAllEntries().map { it.entryId })
        assertEquals(listOf(VersionRecordsEntity("010002", 4)), dao.getAllVersionRecords())
        assertTrue(ftsIds().isEmpty())
        database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM wiki_items_fts WHERE wiki_items_fts MATCH 'replacement'"
        ).use { cursor -> cursor.moveToFirst(); assertEquals(1, cursor.getInt(0)) }
    }

    @Test
    fun successfulReloadCommitsCompleteReplacement() = runBlocking {
        ItemSyncManager(database, validParser()).reload()
        assertEquals(setOf("010001", "010002"), dao.getAllEntries().map { it.entryId }.toSet())
        assertEquals(setOf("010001", "010002"), dao.getAllVersionRecords().map { it.entryId }.toSet())
        assertTrue(ftsIds().isEmpty())
    }
}
