package com.example.photoapp

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoGalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _photos = MutableStateFlow<List<File>>(emptyList())
    val photos: StateFlow<List<File>> = _photos.asStateFlow()

    init {
        scanPhotos()
    }

    fun createPhotoFile(): File {
        val dir = getPhotosDir()
        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        return File(dir, fileName)
    }

    fun onPhotoCaptured(isSuccess: Boolean, file: File?) {
        if (!isSuccess) {
            file?.delete()
            return
        }
        // По требованиям сканируем папку после добавления нового фото.
        scanPhotos()
    }

    fun scanPhotos() {
        val files = getPhotosDir().listFiles().orEmpty()
        _photos.value = files
            .filter { it.isFile && it.extension.equals("jpg", ignoreCase = true) }
            .sortedByDescending { it.lastModified() }
    }

    fun exportToGallery(file: File): Boolean {
        val resolver = getApplication<Application>().contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            val copied = resolver.openOutputStream(uri)?.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
                true
            }
            copied == true
        } catch (_: Exception) {
            false
        }
    }

    private fun getPhotosDir(): File {
        val dir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: getApplication<Application>().filesDir
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
