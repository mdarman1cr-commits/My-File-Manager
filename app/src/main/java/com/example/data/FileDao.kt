package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY dateModified DESC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE category = :category AND isSafe = 0 ORDER BY dateModified DESC")
    fun getFilesByCategory(category: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isStarred = 1 AND isSafe = 0 ORDER BY dateModified DESC")
    fun getStarredFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isSafe = 1 ORDER BY dateModified DESC")
    fun getSafeFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE parentPath = :path AND isSafe = 0 ORDER BY isFolder DESC, name ASC")
    fun getFilesInFolder(path: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE name LIKE '%' || :query || '%' AND isSafe = 0")
    fun searchFiles(query: String): Flow<List<FileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("SELECT SUM(size) FROM files WHERE isSafe = 0")
    fun getTotalStorageUsed(): Flow<Long?>
}
