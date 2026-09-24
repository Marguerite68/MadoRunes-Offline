package com.example.madodict.wiki.data.json

import android.content.Context
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

class ItemJsonParser(private val context: Context) {

    private val gson = Gson()
    private val idPattern = Regex("^\\d{6}$")
    private val categoryByNamespace = mapOf(
        "00" to 2,
        "01" to 1,
        "02" to 0
    )

    // 读取 assets/item/ 下所有JSON文件并解析
    // 返回成功解析的条目列表，解析失败的文件跳过并打印日志
    fun parseAll(): List<EntryJson> {
        val fileNames = try {
            context.assets.list("item") ?: emptyArray()
        } catch (e: Exception) {
            android.util.Log.e("EntryJsonParser", "无法读取item目录", e)
            return emptyList()
        }

        val results = mutableListOf<EntryJson>()
        val seenIds = mutableSetOf<String>()

        fileNames
            .filter { it.endsWith(".json") }
            .forEach { fileName ->
                try {
                    val json = context.assets
                        .open("item/$fileName")
                        .bufferedReader()
                        .readText()
                    val entry = gson.fromJson(json, EntryJson::class.java)

                    val expectedCategory = if (idPattern.matches(entry.id)) {
                        categoryByNamespace[entry.id.take(2)]
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
                        android.util.Log.w(
                            "EntryJsonParser",
                            "条目编号规则校验失败: id=${entry.id}, category=${entry.category}, " +
                                "文件=$fileName, imagePath=${entry.imagePath}，已跳过"
                        )
                        return@forEach
                    }

                    // id 唯一性校验
                    if (!seenIds.add(entry.id)) {
                        android.util.Log.w(
                            "EntryJsonParser",
                            "发现重复 id: ${entry.id}，文件: $fileName，已跳过"
                        )
                        return@forEach
                    }
                    results.add(entry)
                } catch (e: Exception) {
                    android.util.Log.e("EntryJsonParser", "解析失败: $fileName", e)
                }
            }

        return results
    }
}
