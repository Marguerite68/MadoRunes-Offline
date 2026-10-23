package com.example.madodict.wiki

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.madodict.wiki.data.db.WikiDao
import com.example.madodict.wiki.data.db.WikiDatabase
import com.example.madodict.wiki.data.db.WikiItemEntity
import com.example.madodict.wiki.data.json.EntryJson
import com.example.madodict.wiki.data.repository.WikiRepository
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WikiSearchTest {
    private lateinit var database: WikiDatabase
    private lateinit var dao: WikiDao
    private lateinit var repository: WikiRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, WikiDatabase::class.java).build()
        dao = database.encyclopediaDao()
        repository = WikiRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun publishedEntryWithNullEnglishNameIsFoundByContent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entry = context.assets.open("item/000016_Zetsubou_no_majo.json")
            .bufferedReader().use { Gson().fromJson(it, EntryJson::class.java) }
        assertNull(entry.enName)
        assertTrue(entry.content.contains("小圆"))
        dao.insertEntry(WikiItemEntity(
            entry.id, entry.category, entry.name, entry.enName,
            entry.imagePath, entry.content, null, entry.version
        ))

        assertTrue(dao.searchByName("小圆").isEmpty())
        assertEquals(listOf(entry.id), dao.searchByContent("小圆").map { it.entryId })
        assertEquals(listOf(entry.id), repository.searchFull("小圆").map { it.entryId })
    }

    @Test
    fun contentOnlyMatchesIncludeNullEmptyAndNonMatchingEnglishNames() = runBlocking {
        dao.insertEntry(item("null", "A", null))
        dao.insertEntry(item("empty", "B", ""))
        dao.insertEntry(item("other", "C", "Other"))
        dao.insertEntry(item("absent", "D", null, "无关正文"))

        assertEquals(listOf("null", "empty", "other"),
            dao.searchByContent("小圆").map { it.entryId })
        assertTrue(repository.search("小圆").isEmpty())
    }

    @Test
    fun nameAndContentMatchesAreExcludedFromContentResults() = runBlocking {
        dao.insertEntry(item("chinese", "小圆", null))
        dao.insertEntry(item("english", "Other", "小圆"))

        assertEquals(setOf("chinese", "english"),
            dao.searchByName("小圆").map { it.entryId }.toSet())
        assertTrue(dao.searchByContent("小圆").isEmpty())
    }

    @Test
    fun fullSearchKeepsNamesFirstAndReturnsEachEntryOnce() = runBlocking {
        // 正文结果的名称排在最前，确认最终顺序仍由名称命中优先决定。
        dao.insertEntry(item("content", "A", null))
        dao.insertEntry(item("english", "Y", "小圆"))
        dao.insertEntry(item("nameOnly", "Z小圆", null, "无关正文"))
        dao.insertEntry(item("both", "小圆", null))

        val results = repository.searchFull("  小圆  ").map { it.entryId }
        assertEquals(listOf("english", "nameOnly", "both", "content"), results)
        assertEquals(results.size, results.toSet().size)
        assertTrue(repository.searchFull("   ").isEmpty())
    }

    private fun item(
        id: String,
        name: String,
        enName: String?,
        content: String = "正文包含小圆"
    ) = WikiItemEntity(id, 2, name, enName, null, content, null, 1)
}
