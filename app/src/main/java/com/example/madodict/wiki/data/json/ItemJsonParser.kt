package com.example.madodict.wiki.data.json

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// JSON文件的数据结构，与assets中的JSON字段对应
data class EntryJson(
    @SerializedName("id")           val id: String,
    @SerializedName("category")     val category: Int,
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