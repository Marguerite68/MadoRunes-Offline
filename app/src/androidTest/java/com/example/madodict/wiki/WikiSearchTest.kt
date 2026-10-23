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
    fun contentSearchFindsPublishedEntryWithNullEnglishName() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entry = context.assets.open("item/000016_Zetsubou_no_majo.json")
            .bufferedReader().use { Gson().fromJson(it, EntryJson::class.java) }
        assertNull(entry.enName)
        dao.insertEntry(WikiItemEntity(
            entryId = entry.id,
            category = entry.category,
            name = entry.name,
            enName = entry.enName,
            imagePath = entry.imagePath,
            content = entry.content,
            externalLinksJson = null,
            version = entry.version
        ))

        assertEquals(emptyList<String>(), repository.search("小圆").map { it.entryId })
        assertEquals(listOf(entry.id), dao.searchByContent("小圆").map { it.entryId })
        assertEquals(listOf(entry.id), repository.searchFull(" 小圆 ").map { it.entryId })
    }

    @Test
    fun contentOnlyMatchesAllowNullEmptyAndNonMatchingEnglishNames() = runBlocking {
        dao.insertEntry(item("null", "A", null))
        dao.insertEntry(item("empty", "B", ""))
        dao.insertEntry(item("other", "C", "Other"))
        dao.insertEntry(item("miss", "D", null, "无关正文"))

        assertEquals(listOf("null", "empty", "other"),
            dao.searchByContent("小圆").map { it.entryId })
    }

    @Test
    fun nameMatchesAreExcludedFromContentResults() = runBlocking {
        dao.insertEntry(item("chinese", "小圆", null))
        dao.insertEntry(item("english", "B", "小圆"))
        dao.insertEntry(item("both", "小圆外传", "小圆"))

        assertEquals(3, dao.searchByName("小圆").size)
        assertEquals(emptyList<WikiItemEntity>(), dao.searchByContent("小圆"))
        val ids = repository.searchFull("小圆").map { it.entryId }
        assertEquals(3, ids.size)
        assertEquals(3, ids.distinct().size)
    }

    @Test
    fun fullSearchKeepsNameMatchesFirstAndSortsWithinEachGroup() = runBlocking {
        // Content names sort before name matches globally; grouping must take priority.
        dao.insertEntry(item("content-b", "B", "Other"))
        dao.insertEntry(item("name-b", "Z", "小圆", "无关正文"))
        dao.insertEntry(item("content-a", "A", null))
        dao.insertEntry(item("name-a", "Y", "小圆"))

        assertEquals(listOf("name-a", "name-b"),
            repository.search("小圆").map { it.entryId })
        val ids = repository.searchFull("小圆").map { it.entryId }
        assertEquals(listOf("name-a", "name-b", "content-a", "content-b"), ids)
        assertEquals(ids.size, ids.distinct().size)
        assertEquals(emptyList<String>(), repository.searchFull("  ").map { it.entryId })
    }

    private fun item(
        id: String,
        name: String,
        enName: String?,
        content: String = "正文提到小圆"
    ) = WikiItemEntity(
        entryId = id,
        category = 2,
        name = name,
        enName = enName,
        imagePath = null,
        content = content,
        externalLinksJson = null,
        version = 1
    )
}
