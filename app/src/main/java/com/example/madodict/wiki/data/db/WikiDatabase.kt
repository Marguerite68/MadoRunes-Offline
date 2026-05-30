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
    version = 3,
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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS wiki_items_fts
                    USING fts4(content=`wiki_items`, name, enName, content)
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                if (!hasColumn(database, "wiki_items", "enName")) {
                    database.execSQL("ALTER TABLE wiki_items ADD COLUMN enName TEXT")
                }
                database.execSQL("DROP TABLE IF EXISTS wiki_items_fts")
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS wiki_items_fts
                    USING fts4(content=`wiki_items`, name, enName, content)
                """)
                database.execSQL("INSERT INTO wiki_items_fts(wiki_items_fts) VALUES('rebuild')")
            }
        }

        private fun hasColumn(
            database: SupportSQLiteDatabase,
            tableName: String,
            columnName: String
        ): Boolean {
            database.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == columnName) {
                        return true
                    }
                }
            }
            return false
        }
    }
}