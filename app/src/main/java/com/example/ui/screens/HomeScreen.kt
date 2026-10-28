package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.PinEntryDialog
import com.example.ui.components.formatFileSize
import com.example.ui.viewmodel.FileManagerViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FileManagerViewModel,
    totalStorageUsed: Long
) {
    var showSafeFolderPin by remember { mutableStateOf(false) }
    val totalCapacity = 114979147776L // ~107.1 GB in bytes
    val usedFormatted = formatFileSize(totalStorageUsed)
    val totalFormatted = "107.1 GB"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My File",
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Search) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Category Grid Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryItem(
                            icon = Icons.Default.Image,
                            label = "Images",
                            color = Color(0xFF00C853),
                            onClick = { viewModel.navigateTo(Screen.CategoryFiles("Images")) }
                        )
                        CategoryItem(
                            icon = Icons.Default.Movie,
                            label = "Videos",
                            color = Color(0xFF2979FF),
                            onClick = { viewModel.navigateTo(Screen.CategoryFiles("Videos")) }
                        )
                        CategoryItem(
                            icon = Icons.Default.AudioFile,
                            label = "Audio",
                            color = Color(0xFFFF6D00),
                            onClick = { viewModel.navigateTo(Screen.CategoryFiles("Audio")) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryItem(
                            icon = Icons.Default.Description,
                            label = "Document",
                            color = Color(0xFFAA00FF),
                            onClick = { viewModel.navigateTo(Screen.CategoryFiles("Document")) }
                        )
                        CategoryItem(
                            icon = Icons.Default.Download,
                            label = "Download",
                            color = Color(0xFF00B0FF),
                            onClick = { viewModel.navigateTo(Screen.CategoryFiles("Download")) }
                        )
                        CategoryItem(
                            icon = Icons.Default.FolderZip,
                            label = "Zip/APK",
                            color = Color(0xFFE91E63),
                            onClick = { viewModel.navigateTo(Screen.CategoryFiles("Zip/APK")) }
                        )
                    }
                }
            }

            // All Storage Section
            Text(
                text = "All Storage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StorageCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.PhoneAndroid,
                    title = "Internal",
                    subtitle = "$usedFormatted / $totalFormatted",
                    iconColor = Color(0xFF2979FF),
                    onClick = { viewModel.navigateTo(Screen.InternalStorage) }
                )
                StorageCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CenterFocusStrong,
                    title = "Scanner",
                    subtitle = "Smart Scanning",
                    iconColor = Color(0xFF00B0FF),
                    onClick = { viewModel.navigateTo(Screen.Cleanup) }
                )
            }

            // All Tools Section
            Text(
                text = "All Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StorageCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    title = "Starred",
                    subtitle = "Your favourite",
                    iconColor = Color(0xFFFFB300),
                    onClick = { viewModel.navigateTo(Screen.Starred) }
                )
                StorageCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Lock,
                    title = "Safe Folder",
                    subtitle = "Protect your",
                    iconColor = Color(0xFF2979FF),
                    onClick = { showSafeFolderPin = true }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSafeFolderPin) {
        PinEntryDialog(
            onCorrectPin = {
                showSafeFolderPin = false
                viewModel.navigateTo(Screen.SafeFolder)
            },
            onDismiss = { showSafeFolderPin = false }
        )
    }
}

@Composable
fun CategoryItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StorageCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
