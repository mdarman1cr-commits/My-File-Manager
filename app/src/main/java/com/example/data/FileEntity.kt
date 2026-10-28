package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val path: String,
    val size: Long, // in bytes
    val mimeType: String,
    val category: String, // "Images", "Videos", "Audio", "Document", "Download", "Zip/APK"
    val dateModified: Long,
    val isStarred: Boolean = false,
    val isSafe: Boolean = false,
    val isFolder: Boolean = false,
    val parentPath: String = "/",
    val thumbnailUri: String? = null
)
