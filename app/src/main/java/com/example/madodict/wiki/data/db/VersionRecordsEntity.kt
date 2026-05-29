package com.example.madodict.wiki.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "version_records")
data class VersionRecordsEntity (
    @PrimaryKey
    val entryId: String,
    val version: Int
)