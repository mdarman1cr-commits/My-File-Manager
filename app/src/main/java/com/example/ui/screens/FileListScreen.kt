package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileEntity
import com.example.ui.components.FileActionBottomSheet
import com.example.ui.components.FileItemRow
import com.example.ui.viewmodel.FileManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    title: String,
    files: List<FileEntity>,
    viewModel: FileManagerViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedFileForAction by remember { mutableStateOf<FileEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCreateZipDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateZipDialog = true }) {
                        Icon(imageVector = Icons.Default.FolderZip, contentDescription = "Create Zip")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add File")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files found in $title",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                items(files, key = { it.id }) { file ->
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
        FileActionBottomSheet(
            file = file,
            onDismiss = { selectedFileForAction = null },
            onToggleStar = { viewModel.toggleStarred(file) },
            onToggleSafe = { viewModel.moveToSafeFolder(file, !file.isSafe) },
            onRename = { newName -> viewModel.renameFile(file, newName) },
            onDelete = { viewModel.deleteFile(file) },
            onShare = {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, file.name)
                        putExtra(Intent.EXTRA_TEXT, "Sharing file: ${file.name} (${file.path})")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share file via"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to share file", Toast.LENGTH_SHORT).show()
                }
            },
            onExtract = if (file.name.endsWith(".zip") || file.name.endsWith(".rar")) {
                {
                    viewModel.extractZip(file) { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } else null
        )
    }

    if (showAddDialog) {
        var selectedType by remember { mutableStateOf("HTML File") }
        var fileNamePrefix by remember { mutableStateOf("index") }

        val extension = when (selectedType) {
            "Folder" -> ""
            "Empty File" -> ""
            "HTML File" -> ".html"
            "CSS File" -> ".css"
            "JS File" -> ".js"
            "PHP File" -> ".php"
            "PHP Webpage" -> ".php"
            "XML File" -> ".xml"
            "JSON File" -> ".json"
            else -> ""
        }

        val category = if (selectedType == "Folder") "Document" else (if (title == "Internal Storage") "Document" else title)

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val types = listOf("Folder", "Empty File", "HTML File", "CSS File", "JS File", "PHP File", "PHP Webpage", "XML File", "JSON File")
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.chunked(3).forEach { rowTypes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowTypes.forEach { t ->
                                    val isSelected = selectedType == t
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedType = t },
                                        label = { Text(t, fontSize = 11.sp, maxLines = 1) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = fileNamePrefix,
                        onValueChange = { fileNamePrefix = it },
                        label = { Text("File Name") },
                        suffix = { Text(extension) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (fileNamePrefix.isNotBlank()) {
                        val fullName = if (selectedType == "Folder" || extension.isEmpty()) fileNamePrefix else "$fileNamePrefix$extension"
                        val mime = when (extension) {
                            ".html" -> "text/html"
                            ".css" -> "text/css"
                            ".js" -> "application/javascript"
                            ".php" -> "application/x-httpd-php"
                            ".xml" -> "application/xml"
                            ".json" -> "application/json"
                            else -> if (selectedType == "Folder") "inode/directory" else "text/plain"
                        }
                        viewModel.addNewFile(
                            name = fullName,
                            category = category,
                            size = if (selectedType == "Folder") 0L else 512L,
                            mimeType = mime
                        )
                    }
                    showAddDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreateZipDialog) {
        var zipName by remember { mutableStateOf("Archive_Combined.zip") }

        AlertDialog(
            onDismissRequest = { showCreateZipDialog = false },
            title = { Text("Create Zip Archive") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Combine files into a compressed zip archive:")
                    OutlinedTextField(
                        value = zipName,
                        onValueChange = { zipName = it },
                        label = { Text("Archive Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (zipName.isNotBlank()) {
                        viewModel.createZip(zipName) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    showCreateZipDialog = false
                }) {
                    Text("Create Zip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateZipDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
