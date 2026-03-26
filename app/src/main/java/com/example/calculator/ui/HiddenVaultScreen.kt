package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.BackHandler
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import android.net.Uri
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import com.example.calculator.FileType
import com.example.calculator.HiddenVaultManager
import com.example.calculator.VaultFile
import java.io.File

@Composable
fun HiddenVaultScreen(
    vaultManager: HiddenVaultManager,
    onExitVault: () -> Unit,
    onBackPressed: () -> Unit = {}
) {
    var vaultFiles by remember { mutableStateOf(vaultManager.getVaultFiles()) }
    var showAddFileDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<VaultFile?>(null) }
    var selectedFileType by remember { mutableStateOf<FileType?>(null) }

    val context = LocalContext.current
    val galleryImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    // Handle back button based on vault navigation state
    BackHandler {
        when {
            selectedFile != null -> {
                // If viewing a file, go back to file list
                selectedFile = null
            }
            selectedFileType != null -> {
                // If viewing a file type, go back to albums
                selectedFileType = null
            }
            else -> {
                // If on main albums screen, exit vault
                onBackPressed()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        if (selectedFile != null) {
            FileViewerScreen(
                file = selectedFile!!,
                vaultManager = vaultManager,
                imageLoader = galleryImageLoader,
                onBack = { selectedFile = null }
            )
        } else if (selectedFileType != null) {
            FolderFilesScreen(
                type = selectedFileType!!,
                vaultManager = vaultManager,
                imageLoader = galleryImageLoader,
                onBack = { selectedFileType = null },
                onOpenFile = { selectedFile = it },
                onRefresh = { vaultFiles = vaultManager.getVaultFiles() }
            )
        } else {
            // Main Albums Screen (Gallery Style)
            val density = LocalDensity.current
            val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)
            val navBarHeight = with(density) { navBarHeightPx.toDp() }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = navBarHeight)
            ) {
                Spacer(modifier = Modifier.height(64.dp))
                
                // Title "Albums"
                Text(
                    text = "Albums",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.W500,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Action Icons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showAddFileDialog = true }
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Essential albums",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "View all",
                        fontSize = 16.sp,
                        color = Color(0xFF4C6BFF),
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.clickable { /* View all action */ }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Grid of Albums (Only 3 folders: Pictures, Videos, Files)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Pictures Folder
                    item {
                        val files = vaultManager.getFilesByType(FileType.IMAGE)
                        AlbumCard(
                            title = "Pictures",
                            count = files.size,
                            previewFile = files.firstOrNull(),
                            vaultManager = vaultManager,
                            imageLoader = galleryImageLoader,
                            onClick = { selectedFileType = FileType.IMAGE }
                        )
                    }
                    
                    // Videos Folder
                    item {
                        val files = vaultManager.getFilesByType(FileType.VIDEO)
                        AlbumCard(
                            title = "Videos",
                            count = files.size,
                            previewFile = files.firstOrNull(),
                            vaultManager = vaultManager,
                            imageLoader = galleryImageLoader,
                            onClick = { selectedFileType = FileType.VIDEO }
                        )
                    }
                    
                    // Files Folder
                    item {
                        val files = vaultManager.getFilesByType(FileType.FILE)
                        AlbumCard(
                            title = "Files",
                            count = files.size,
                            previewFile = null,
                            vaultManager = vaultManager,
                            imageLoader = galleryImageLoader,
                            onClick = { selectedFileType = FileType.FILE }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Exit button
                Text(
                    text = "Exit Vault",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                        .clickable { onExitVault() }
                )
            }
        }
    }

    if (showAddFileDialog) {
        AddFileDialog(
            onSave = { fileName, content ->
                vaultManager.saveFile(fileName, content)
                vaultFiles = vaultManager.getVaultFiles()
                showAddFileDialog = false
            },
            onDismiss = { showAddFileDialog = false }
        )
    }
}

@Composable
fun AlbumCard(
    title: String,
    count: Int,
    previewFile: VaultFile?,
    vaultManager: HiddenVaultManager,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1C1C1E)),
            contentAlignment = Alignment.Center
        ) {
            if (previewFile != null && (previewFile.type == FileType.IMAGE || previewFile.type == FileType.VIDEO)) {
                val filePath = vaultManager.getFileUri(previewFile.name)
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = File(filePath),
                            imageLoader = imageLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (previewFile.type == FileType.VIDEO) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = when (title) {
                        "Videos" -> "🎬"
                        "Files" -> "📄"
                        else -> "🖼️"
                    },
                    fontSize = 32.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1
        )
        
        Text(
            text = count.toString(),
            color = Color(0xFF8E8E93),
            fontSize = 13.sp
        )
    }
}


@Composable
fun FolderFilesScreen(
    type: FileType,
    vaultManager: HiddenVaultManager,
    imageLoader: ImageLoader,
    onBack: () -> Unit,
    onOpenFile: (VaultFile) -> Unit,
    onRefresh: () -> Unit
) {
    var selectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)
    val navBarHeight = with(density) { navBarHeightPx.toDp() }
    
    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(bottom = navBarHeight)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (selectionMode) {
                        selectionMode = false
                        selectedItems = emptySet()
                    } else {
                        onBack()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = if (selectionMode) "${selectedItems.size} selected" else type.toString().lowercase().replaceFirstChar { it.uppercase() } + "s",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            if (selectionMode && selectedItems.isNotEmpty()) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Text("🗑️", fontSize = 20.sp)
                }
            }
        }

        val files = vaultManager.getFilesByType(type)
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No files in this folder", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files) { file ->
                    VaultFileGridItem(
                        file = file,
                        vaultManager = vaultManager,
                        imageLoader = imageLoader,
                        isSelected = selectedItems.contains(file.name),
                        selectionMode = selectionMode,
                        onSelectionToggle = {
                            selectedItems = if (selectedItems.contains(file.name)) {
                                selectedItems - file.name
                            } else {
                                selectedItems + file.name
                            }
                            if (selectedItems.isEmpty()) {
                                selectionMode = false
                            }
                        },
                        onLongPress = {
                            selectionMode = true
                            selectedItems = setOf(file.name)
                        },
                        onDelete = {
                            vaultManager.deleteFile(file.name)
                            onRefresh()
                        },
                        onOpen = { 
                            if (!selectionMode) {
                                onOpenFile(file)
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Delete ${selectedItems.size} item${if (selectedItems.size > 1) "s" else ""}?", color = Color.White) },
            text = { Text("This action cannot be undone.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        selectedItems.forEach { fileName ->
                            vaultManager.deleteFile(fileName)
                        }
                        selectedItems = emptySet()
                        selectionMode = false
                        showDeleteConfirm = false
                        onRefresh()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VaultFileGridItem(
    file: VaultFile,
    vaultManager: HiddenVaultManager,
    imageLoader: ImageLoader,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onSelectionToggle: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDelete: () -> Unit = {},
    onOpen: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
            .combinedClickable(
                onClick = { if (!selectionMode) onOpen() },
                onLongClick = { onLongPress() },
                enabled = true,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (file.type == FileType.IMAGE || file.type == FileType.VIDEO) {
            val filePath = vaultManager.getFileUri(file.name)
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = File(filePath),
                        imageLoader = imageLoader
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (file.type == FileType.VIDEO) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        } else {
            Text(
                text = "📄",
                fontSize = 24.sp
            )
        }
        
        // Selection checkbox (shown in selection mode)
        if (selectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelected) Color.Black.copy(alpha = 0.6f) else Color.Transparent
                    )
                    .clickable { onSelectionToggle() }
            )
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionToggle() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50),
                    uncheckedColor = Color.Gray
                )
            )
        } else {
            // Simple delete button (shown in normal mode)
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Text("×", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AddFileDialog(
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        title = { Text("Add New File", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileContent,
                    onValueChange = { fileContent = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (fileName.isNotBlank()) onSave(fileName, fileContent) }) {
                Text("Save", color = Color(0xFF4C6BFF))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun FileViewerScreen(
    file: VaultFile,
    vaultManager: HiddenVaultManager,
    imageLoader: ImageLoader,
    onBack: () -> Unit
) {
    var isPlayingVideo by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val density = LocalDensity.current
    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)
    val navBarHeight = with(density) { navBarHeightPx.toDp() }
    
    when {
        isPlayingVideo && file.type == FileType.VIDEO -> {
            VideoPlayerScreen(
                file = file,
                vaultManager = vaultManager,
                onBack = { isPlayingVideo = false },
                onExit = onBack
            )
        }
        else -> {
            Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(bottom = navBarHeight)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(file.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (file.type) {
                        FileType.IMAGE -> {
                            val filePath = vaultManager.getFileUri(file.name)
                            Image(
                                painter = rememberAsyncImagePainter(File(filePath)),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        FileType.VIDEO -> {
                            val filePath = vaultManager.getFileUri(file.name)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { isPlayingVideo = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = File(filePath),
                                        imageLoader = imageLoader
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play video",
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        FileType.FILE -> {
                            Text(vaultManager.readFile(file.name) ?: "", color = Color.White, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(
    file: VaultFile,
    vaultManager: HiddenVaultManager,
    onBack: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val filePath = vaultManager.getFileUri(file.name)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.fromFile(File(filePath)))
            setMediaItem(mediaItem)
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Handle back button to exit video player
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit", tint = Color.White)
            }
            Text(file.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        // Video Player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

