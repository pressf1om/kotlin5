package com.example.photoapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryApp(viewModel: PhotoGalleryViewModel = viewModel()) {
    val context = LocalContext.current
    val photos by viewModel.photos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        viewModel.onPhotoCaptured(success, pendingPhotoFile)
        pendingPhotoFile = null
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        if (granted) {
            val file = viewModel.createPhotoFile()
            pendingPhotoFile = file
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            takePictureLauncher.launch(uri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Нет разрешений для камеры")
            }
        }
    }

    fun launchCamera() {
        val required = buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val missing = required.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            val file = viewModel.createPhotoFile()
            pendingPhotoFile = file
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            takePictureLauncher.launch(uri)
        } else {
            permissionsLauncher.launch(missing.toTypedArray())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Личная галерея") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = ::launchCamera) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Сделать фото")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (photos.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onTakePhoto = ::launchCamera
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(photos, key = { it.name }) { file ->
                    PhotoItem(
                        file = file,
                        onExport = {
                            val ok = viewModel.exportToGallery(file)
                            scope.launch {
                                if (ok) {
                                    snackbarHostState.showSnackbar("Фото добавлено в галерею")
                                } else {
                                    snackbarHostState.showSnackbar("Не удалось экспортировать фото")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, onTakePhoto: () -> Unit) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "У вас пока нет фото",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onTakePhoto) {
            Text("Сделать первое фото")
        }
    }
}

@Composable
private fun PhotoItem(file: File, onExport: () -> Unit) {
    val bitmap = remember(file.absolutePath) {
        BitmapFactory.decodeFile(file.absolutePath)
    }
    var menuExpanded by remember(file.name) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет превью")
                }
            }

            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню"
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Экспорт в галерею") },
                    onClick = {
                        menuExpanded = false
                        onExport()
                    }
                )
            }
        }
    }
}
