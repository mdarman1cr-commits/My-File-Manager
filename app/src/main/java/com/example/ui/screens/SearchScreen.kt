package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.FileEntity
import com.example.ui.components.FileActionBottomSheet
import com.example.ui.components.FileItemRow
import com.example.ui.viewmodel.FileManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    results: List<FileEntity>,
    viewModel: FileManagerViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var selectedFileForAction by remember { mutableStateOf<FileEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search files...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (results.isEmpty() && query.isNotBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No files found matching \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(results, key = { it.id }) { file ->
                    FileItemRow(
                        file = file,
                        onClick = { selectedFileForAction = file },
                        onMoreClick = { selectedFileForAction = file }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    selectedFileForAction?.let { file ->
        val context = androidx.compose.ui.platform.LocalContext.current
        FileActionBottomSheet(
            file = file,
            onDismiss = { selectedFileForAction = null },
            onToggleStar = { viewModel.toggleStarred(file) },
            onToggleSafe = { viewModel.moveToSafeFolder(file, !file.isSafe) },
            onRename = { newName -> viewModel.renameFile(file, newName) },
            onDelete = { viewModel.deleteFile(file) },
            onOpenWith = {
                try {
                    val fileObj = java.io.File(file.path)
                    val uri = if (fileObj.exists()) {
                        androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            fileObj
                        )
                    } else {
                        android.net.Uri.parse(file.path)
                    }
                    val viewIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, file.mimeType.ifEmpty { "*/*" })
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(viewIntent, "Open with"))
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "No app available to open this file", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onShare = {
                try {
                    val fileObj = java.io.File(file.path)
                    val uri = if (fileObj.exists()) {
                        androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            fileObj
                        )
                    } else {
                        null
                    }
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = file.mimeType.ifEmpty { "*/*" }
                        if (uri != null) {
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        } else {
                            putExtra(android.content.Intent.EXTRA_TEXT, "Sharing file: ${file.name}")
                        }
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share file via"))
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Unable to share file", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onExtract = if (file.name.endsWith(".zip")) {
                {
                    viewModel.extractZip(file) { msg ->
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } else null
        )
    }
}
