package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileEntity
import java.text.DecimalFormat
import java.util.Date

fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
    val df = DecimalFormat("#,##0.#")
    return "${df.format(sizeBytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
}

@Composable
fun getFileIcon(mimeType: String, category: String): Pair<ImageVector, Color> {
    return when {
        mimeType.startsWith("image/") -> Icons.Default.Image to Color(0xFF00C853)
        mimeType.startsWith("video/") -> Icons.Default.Movie to Color(0xFF2979FF)
        mimeType.startsWith("audio/") -> Icons.Default.AudioFile to Color(0xFFFF6D00)
        mimeType.contains("pdf") || mimeType.contains("document") || mimeType.contains("sheet") -> Icons.Default.Description to Color(0xFFAA00FF)
        mimeType.contains("zip") || mimeType.contains("rar") || mimeType.contains("compressed") -> Icons.Default.FolderZip to Color(0xFFE91E63)
        mimeType.contains("android.package-archive") -> Icons.Default.Android to Color(0xFFE91E63)
        category == "Images" -> Icons.Default.Image to Color(0xFF00C853)
        category == "Videos" -> Icons.Default.Movie to Color(0xFF2979FF)
        category == "Audio" -> Icons.Default.AudioFile to Color(0xFFFF6D00)
        category == "Document" -> Icons.Default.Description to Color(0xFFAA00FF)
        category == "Download" -> Icons.Default.Download to Color(0xFF00B0FF)
        category == "Zip/APK" -> Icons.Default.FolderZip to Color(0xFFE91E63)
        else -> Icons.Default.InsertDriveFile to Color(0xFF78909C)
    }
}

@Composable
fun FileItemRow(
    file: FileEntity,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = getFileIcon(file.mimeType, file.category)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = formatFileSize(file.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(Date(file.dateModified)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (file.isStarred) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Starred",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionBottomSheet(
    file: FileEntity,
    onDismiss: () -> Unit,
    onToggleStar: () -> Unit,
    onToggleSafe: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExtract: (() -> Unit)? = null
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(file.name) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            if ((file.name.endsWith(".zip") || file.name.endsWith(".rar")) && onExtract != null) {
                NavigationDrawerItem(
                    label = { Text("Extract Zip") },
                    selected = false,
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                    onClick = {
                        onExtract()
                        onDismiss()
                    }
                )
            }
            NavigationDrawerItem(
                label = { Text("Share") },
                selected = false,
                icon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = {
                    onShare()
                    onDismiss()
                }
            )
            NavigationDrawerItem(
                label = { Text(if (file.isStarred) "Remove Star" else "Add to Starred") },
                selected = false,
                icon = { Icon(if (file.isStarred) Icons.Default.Star else Icons.Outlined.StarBorder, contentDescription = null) },
                onClick = {
                    onToggleStar()
                    onDismiss()
                }
            )
            NavigationDrawerItem(
                label = { Text(if (file.isSafe) "Remove from Safe Folder" else "Move to Safe Folder") },
                selected = false,
                icon = { Icon(if (file.isSafe) Icons.Default.LockOpen else Icons.Outlined.Lock, contentDescription = null) },
                onClick = {
                    onToggleSafe()
                    onDismiss()
                }
            )
            NavigationDrawerItem(
                label = { Text("Rename") },
                selected = false,
                icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = { showRenameDialog = true }
            )
            NavigationDrawerItem(
                label = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                selected = false,
                icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    singleLine = true,
                    label = { Text("File Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newNameInput.isNotBlank()) {
                        onRename(newNameInput)
                    }
                    showRenameDialog = false
                    onDismiss()
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PinEntryDialog(
    onCorrectPin: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Safe Folder PIN") },
        text = {
            Column {
                Text("Default PIN is 1234")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        error = false
                    },
                    label = { Text("4-digit PIN") },
                    singleLine = true,
                    isError = error
                )
                if (error) {
                    Text("Incorrect PIN. Try 1234", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (pin == "1234") {
                    onCorrectPin()
                } else {
                    error = true
                }
            }) {
                Text("Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
