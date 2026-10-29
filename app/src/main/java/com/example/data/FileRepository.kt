package com.example.data

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.flow.Flow
import java.io.File

class FileRepository(private val fileDao: FileDao) {
    val allFiles: Flow<List<FileEntity>> = fileDao.getAllFiles()
    val starredFiles: Flow<List<FileEntity>> = fileDao.getStarredFiles()
    val safeFiles: Flow<List<FileEntity>> = fileDao.getSafeFiles()
    val totalStorageUsed: Flow<Long?> = fileDao.getTotalStorageUsed()

    fun getFilesByCategory(category: String): Flow<List<FileEntity>> = fileDao.getFilesByCategory(category)
    fun getFilesInFolder(path: String): Flow<List<FileEntity>> = fileDao.getFilesInFolder(path)
    fun searchFiles(query: String): Flow<List<FileEntity>> = fileDao.searchFiles(query)

    suspend fun insertFile(file: FileEntity) = fileDao.insertFile(file)
    suspend fun updateFile(file: FileEntity) = fileDao.updateFile(file)
    suspend fun deleteFile(file: FileEntity) = fileDao.deleteFile(file)

    suspend fun scanRealDeviceStorage(context: Context) {
        try {
            val rootDirs = listOf(
                Environment.getExternalStorageDirectory(),
                context.getExternalFilesDir(null),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            )

            val scannedFiles = mutableListOf<FileEntity>()
            for (dir in rootDirs) {
                if (dir != null && dir.exists() && dir.isDirectory) {
                    dir.walkTopDown().maxDepth(3).forEach { file ->
                        if (file.isFile) {
                            val name = file.name
                            val size = file.length()
                            val path = file.absolutePath
                            val mime = when {
                                name.endsWith(".jpg", true) || name.endsWith(".png", true) -> "image/jpeg"
                                name.endsWith(".mp4", true) || name.endsWith(".mkv", true) -> "video/mp4"
                                name.endsWith(".mp3", true) || name.endsWith(".wav", true) -> "audio/mpeg"
                                name.endsWith(".pdf", true) -> "application/pdf"
                                name.endsWith(".zip", true) || name.endsWith(".rar", true) -> "application/zip"
                                name.endsWith(".html", true) -> "text/html"
                                name.endsWith(".css", true) -> "text/css"
                                name.endsWith(".js", true) -> "application/javascript"
                                name.endsWith(".php", true) -> "application/x-httpd-php"
                                name.endsWith(".xml", true) -> "application/xml"
                                name.endsWith(".json", true) -> "application/json"
                                else -> "text/plain"
                            }
                            val category = when {
                                mime.startsWith("image/") -> "Images"
                                mime.startsWith("video/") -> "Videos"
                                mime.startsWith("audio/") -> "Audio"
                                mime.contains("pdf") || mime.contains("document") || mime.contains("sheet") || mime.contains("html") || mime.contains("css") || mime.contains("xml") || mime.contains("json") -> "Document"
                                path.contains("Download", true) -> "Download"
                                name.endsWith(".zip", true) || name.endsWith(".rar", true) || name.endsWith(".apk", true) -> "Zip/APK"
                                else -> "Document"
                            }
                            scannedFiles.add(
                                FileEntity(
                                    name = name,
                                    path = path,
                                    size = size,
                                    mimeType = mime,
                                    category = category,
                                    dateModified = file.lastModified()
                                )
                            )
                        }
                    }
                }
            }
            if (scannedFiles.isNotEmpty()) {
                fileDao.insertFiles(scannedFiles.distinctBy { it.path })
            }
        } catch (e: Exception) {
            // Never crash
        }
    }
}
