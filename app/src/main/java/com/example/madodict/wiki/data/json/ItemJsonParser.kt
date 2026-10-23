package com.example.madodict.wiki.data.json

import android.content.Context
import com.example.madodict.wiki.data.WikiCategory
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// JSON文件的数据结构，与assets中的JSON字段对应
data class EntryJson(
    @SerializedName("id")           val id: String,
    @SerializedName("category")     val category: Int,
    @SerializedName("enName")      val enName: String?,
    @SerializedName("name")         val name: String,
    @SerializedName("imagePath")    val imagePath: String?,
    @SerializedName("content")      val content: String,
    @SerializedName("externalLinks") val externalLinks: List<ExternalLinkJson>?,
    @SerializedName("version")      val version: Int
)

data class ExternalLinkJson(
    @SerializedName("label") val label: String,
    @SerializedName("url")   val url: String
)

class ItemJsonParser internal constructor(
    private val listFiles: () -> Array<String>?,
    private val readFile: (String) -> String
) {
    constructor(context: Context) : this(
        listFiles = { context.assets.list("item") },
        readFile = { fileName ->
            context.assets.open("item/$fileName").bufferedReader().use { it.readText() }
        }
    )

    private val gson = Gson()
    private val idPattern = Regex("^\\d{6}$")

    // 只有全部资源读取、解析和校验成功，才允许调用方据此更新或删除数据库条目。
    fun parseAll(): List<EntryJson> {
        val fileNames = try {
            checkNotNull(listFiles()) { "无法读取item目录" }
                .filter { it.endsWith(".json") }
                .also { check(it.isNotEmpty()) { "item目录没有JSON词条，拒绝清空数据库" } }
        } catch (e: Exception) {
            throw IllegalStateException("无法读取完整的item资源", e)
        }

        val results = mutableListOf<EntryJson>()
        val seenIds = mutableSetOf<String>()

        fileNames.forEach { fileName ->
            try {
                val entry = checkNotNull(gson.fromJson(readFile(fileName), EntryJson::class.java)) {
                    "JSON词条不能为空"
                }
                // Gson可能将缺失的非空字段反序列化为null，必须在写入数据库前拒绝。
                require(!entry.id.isNullOrBlank() && !entry.name.isNullOrBlank() &&
                    !entry.content.isNullOrBlank()) { "词条缺少必要字段" }

                val expectedCategory = if (idPattern.matches(entry.id)) {
                    WikiCategory.fromNamespace(entry.id.take(2))?.value
                } else {
                    null
                }
                val imagePathMatchesId = entry.imagePath == null ||
                    entry.imagePath.startsWith("${entry.id}_")
                if (
                    expectedCategory == null ||
                    entry.category != expectedCategory ||
                    !fileName.startsWith("${entry.id}_") ||
                    !imagePathMatchesId
                ) {
                    error("条目编号规则校验失败: id=${entry.id}, category=${entry.category}, " +
                        "文件=$fileName, imagePath=${entry.imagePath}")
                }

                check(seenIds.add(entry.id)) { "发现重复id: ${entry.id}，文件: $fileName" }
                results.add(entry)
            } catch (e: Exception) {
                throw IllegalStateException("解析失败: $fileName", e)
            }
        }

        return results
    }
}
