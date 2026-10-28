package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FileEntity
import com.example.data.FileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class CategoryFiles(val categoryName: String) : Screen()
    object InternalStorage : Screen()
    object Starred : Screen()
    object SafeFolder : Screen()
    object Cleanup : Screen()
    object Settings : Screen()
    object Search : Screen()
}

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FileRepository

    init {
        val dao = AppDatabase.getDatabase(application).fileDao()
        repository = FileRepository(dao)
        viewModelScope.launch {
            val all = dao.getAllFiles().first()
            if (all.isEmpty()) {
                repository.insertDefaultFiles()
            }
        }
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val allFiles: StateFlow<List<FileEntity>> = repository.allFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val starredFiles: StateFlow<List<FileEntity>> = repository.starredFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safeFiles: StateFlow<List<FileEntity>> = repository.safeFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStorageUsed: StateFlow<Long> = repository.totalStorageUsed
        .map { it ?: 29957314560L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 29957314560L)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<FileEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchFiles(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleStarred(file: FileEntity) {
        viewModelScope.launch {
            repository.updateFile(file.copy(isStarred = !file.isStarred))
        }
    }

    fun moveToSafeFolder(file: FileEntity, isSafe: Boolean) {
        viewModelScope.launch {
            repository.updateFile(file.copy(isSafe = isSafe))
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
        }
    }

    fun renameFile(file: FileEntity, newName: String) {
        viewModelScope.launch {
            repository.updateFile(file.copy(name = newName))
        }
    }

    fun addNewFile(name: String, category: String, size: Long, mimeType: String) {
        viewModelScope.launch {
            val newFile = FileEntity(
                name = name,
                path = "/Internal/$category/$name",
                size = size,
                mimeType = mimeType,
                category = category,
                dateModified = System.currentTimeMillis()
            )
            repository.insertFile(newFile)
        }
    }

    fun extractZip(file: FileEntity, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val extractedName = file.name.removeSuffix(".zip").removeSuffix(".rar") + "_extracted"
            val newFile = FileEntity(
                name = extractedName,
                path = "/Internal/Document/$extractedName",
                size = file.size * 2,
                mimeType = "application/octet-stream",
                category = "Document",
                dateModified = System.currentTimeMillis()
            )
            repository.insertFile(newFile)
            onComplete("Extracted ${file.name} successfully!")
        }
    }

    fun createZip(zipName: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val finalName = if (zipName.endsWith(".zip")) zipName else "$zipName.zip"
            val newZip = FileEntity(
                name = finalName,
                path = "/Internal/Zip/APK/$finalName",
                size = 2500000L,
                mimeType = "application/zip",
                category = "Zip/APK",
                dateModified = System.currentTimeMillis()
            )
            repository.insertFile(newZip)
            onComplete("Created archive $finalName successfully!")
        }
    }
}
