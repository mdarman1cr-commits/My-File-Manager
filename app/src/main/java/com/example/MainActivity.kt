package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FileManagerViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: FileManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var hasPermission by remember {
                        mutableStateOf(checkStoragePermission())
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { _ ->
                        hasPermission = checkStoragePermission()
                    }

                    if (!hasPermission) {
                        // Blocking permission screen on startup if permission is not granted
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Storage Permission Required",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "To use My File and manage your files securely, please grant storage permission. Once granted, you won't be asked again.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(28.dp))
                                Button(
                                    onClick = {
                                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            arrayOf(
                                                Manifest.permission.READ_MEDIA_IMAGES,
                                                Manifest.permission.READ_MEDIA_VIDEO,
                                                Manifest.permission.READ_MEDIA_AUDIO
                                            )
                                        } else {
                                            arrayOf(
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            )
                                        }
                                        permissionLauncher.launch(permissions)
                                    },
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    Text("Grant Permission", fontSize = 16.sp)
                                }
                            }
                        }
                    } else {
                        // Main App Content or External File Viewer
                        val viewingExternalFile by viewModel.viewingExternalFile.collectAsState()
                        if (viewingExternalFile != null) {
                            val extFile = viewingExternalFile!!
                            when {
                                extFile.mimeType.startsWith("image/") || extFile.name.endsWith(".jpg", true) || extFile.name.endsWith(".png", true) || extFile.name.endsWith(".jpeg", true) -> {
                                    ImageViewerScreen(file = extFile, viewModel = viewModel, onBack = { viewModel.clearExternalFile() })
                                }
                                extFile.mimeType.contains("pdf") || extFile.name.endsWith(".pdf", true) -> {
                                    PdfViewerScreen(file = extFile, viewModel = viewModel, onBack = { viewModel.clearExternalFile() })
                                }
                                extFile.mimeType.contains("zip") || extFile.mimeType.contains("compressed") || extFile.name.endsWith(".zip", true) || extFile.name.endsWith(".rar", true) -> {
                                    ZipViewerScreen(file = extFile, viewModel = viewModel, onBack = { viewModel.clearExternalFile() })
                                }
                                else -> {
                                    CodeEditorScreen(file = extFile, viewModel = viewModel, onBack = { viewModel.clearExternalFile() })
                                }
                            }
                        } else {
                            val currentScreen by viewModel.currentScreen.collectAsState()
                            val allFiles by viewModel.allFiles.collectAsState()
                            val starredFiles by viewModel.starredFiles.collectAsState()
                            val safeFiles by viewModel.safeFiles.collectAsState()
                            val totalStorageUsed by viewModel.totalStorageUsed.collectAsState()
                            val searchQuery by viewModel.searchQuery.collectAsState()
                            val searchResults by viewModel.searchResults.collectAsState()

                            when (val screen = currentScreen) {
                                is Screen.Home -> HomeScreen(viewModel, totalStorageUsed)
                                is Screen.CategoryFiles -> FileListScreen(
                                    title = screen.categoryName,
                                    files = allFiles.filter { it.category == screen.categoryName && !it.isSafe },
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(Screen.Home) }
                                )
                                is Screen.InternalStorage -> FileListScreen(
                                    title = "Internal Storage",
                                    files = allFiles.filter { !it.isSafe },
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(Screen.Home) }
                                )
                                is Screen.Starred -> StarredScreen(
                                    starredFiles = starredFiles,
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(Screen.Home) }
                                )
                                is Screen.SafeFolder -> SafeFolderScreen(
                                    safeFiles = safeFiles,
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(Screen.Home) }
                                )
                                is Screen.Cleanup -> CleanupScreen(
                                    totalUsed = totalStorageUsed,
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(Screen.Home) }
                                )
                                is Screen.Settings -> SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(Screen.Home) }
                                )
                                is Screen.Search -> SearchScreen(
                                    query = searchQuery,
                                    results = searchResults,
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(Screen.Home) }
                                )
                                else -> HomeScreen(viewModel, totalStorageUsed)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type
        val uri = intent.data ?: intent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
        if (uri != null) {
            viewModel.openExternalFileUri(this, uri, type)
        }
    }

    private fun checkStoragePermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            true
        }
    }
}
