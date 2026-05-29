package com.example.madodict.wiki.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.jvm.java

@Database(
    entities = [
        WikiItemEntity::class,
        VersionRecordsEntity::class,
        WikiItemFtsEntity::class
               ],
    version = 2,
    exportSchema = false
)
abstract class WikiDatabase : RoomDatabase() {

    abstract fun encyclopediaDao(): WikiDao

    companion object {
        @Volatile
        private var INSTANCE: WikiDatabase? = null

        fun getInstance(context: Context): WikiDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    WikiDatabase::class.java,
                    "encyclopedia.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS wiki_items_fts
                    USING fts4(content=`wiki_items`, name, content)
                """)
            }
        }
    }
}