package com.example.data

import kotlinx.coroutines.flow.Flow

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

    suspend fun insertDefaultFiles() {
        val now = System.currentTimeMillis()
        val defaults = listOf(
            FileEntity(name = "Vacation_2026.jpg", path = "/Internal/Images/Vacation_2026.jpg", size = 3200000L, mimeType = "image/jpeg", category = "Images", dateModified = now - 100000L, isStarred = true),
            FileEntity(name = "Sunset_Beach.png", path = "/Internal/Images/Sunset_Beach.png", size = 2400000L, mimeType = "image/png", category = "Images", dateModified = now - 200000L),
            FileEntity(name = "Family_Portrait.jpg", path = "/Internal/Images/Family_Portrait.jpg", size = 4100000L, mimeType = "image/jpeg", category = "Images", dateModified = now - 300000L),
            
            FileEntity(name = "Project_Overview.mp4", path = "/Internal/Videos/Project_Overview.mp4", size = 128000000L, mimeType = "video/mp4", category = "Videos", dateModified = now - 400000L, isStarred = true),
            FileEntity(name = "Tutorial_Kotlin.mkv", path = "/Internal/Videos/Tutorial_Kotlin.mkv", size = 450000000L, mimeType = "video/x-matroska", category = "Videos", dateModified = now - 500000L),
            
            FileEntity(name = "Acoustic_Guitar.mp3", path = "/Internal/Audio/Acoustic_Guitar.mp3", size = 8500000L, mimeType = "audio/mpeg", category = "Audio", dateModified = now - 600000L, isStarred = true),
            FileEntity(name = "Podcast_Ep12.wav", path = "/Internal/Audio/Podcast_Ep12.wav", size = 42000000L, mimeType = "audio/wav", category = "Audio", dateModified = now - 700000L),
            
            FileEntity(name = "Project_Report.pdf", path = "/Internal/Document/Project_Report.pdf", size = 1500000L, mimeType = "application/pdf", category = "Document", dateModified = now - 800000L, isStarred = true),
            FileEntity(name = "Budget_Q3.xlsx", path = "/Internal/Document/Budget_Q3.xlsx", size = 850000L, mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", category = "Document", dateModified = now - 900000L),
            FileEntity(name = "Meeting_Notes.docx", path = "/Internal/Document/Meeting_Notes.docx", size = 450000L, mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document", category = "Document", dateModified = now - 950000L),
            
            FileEntity(name = "Setup_Installer.zip", path = "/Internal/Download/Setup_Installer.zip", size = 35000000L, mimeType = "application/zip", category = "Download", dateModified = now - 1100000L),
            FileEntity(name = "Sample_App.apk", path = "/Internal/Zip/APK/Sample_App.apk", size = 18000000L, mimeType = "application/vnd.android.package-archive", category = "Zip/APK", dateModified = now - 1200000L, isStarred = true),
            FileEntity(name = "Archive_Docs.rar", path = "/Internal/Zip/APK/Archive_Docs.rar", size = 22000000L, mimeType = "application/x-rar-compressed", category = "Zip/APK", dateModified = now - 1300000L)
        )
        fileDao.insertFiles(defaults)
    }
}
