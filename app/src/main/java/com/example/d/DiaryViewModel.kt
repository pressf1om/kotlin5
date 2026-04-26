package com.example.d

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    init {
        // Полное сканирование выполняется только один раз при старте ViewModel.
        _entries.value = loadEntriesOnce()
    }

    fun saveEntry(title: String, text: String, existingEntry: DiaryEntry? = null) {
        val cleanTitle = title.trim()
        val preview = text.trim().take(40)

        if (existingEntry == null) {
            val timestamp = System.currentTimeMillis()
            val fileTitle = sanitizeTitleForFile(cleanTitle)
            val fileName = "${timestamp}_${fileTitle}.txt"
            val file = File(getApplication<Application>().filesDir, fileName)
            file.writeText(text)

            val newEntry = DiaryEntry(
                file = file,
                timestamp = timestamp,
                title = cleanTitle,
                preview = preview
            )

            _entries.value = listOf(newEntry) + _entries.value
        } else {
            existingEntry.file.writeText(text)
            val updated = existingEntry.copy(
                title = cleanTitle,
                preview = preview
            )

            _entries.value = _entries.value.map { entry ->
                if (entry.file.name == existingEntry.file.name) updated else entry
            }
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        entry.file.delete()
        _entries.value = _entries.value.filter { it.file.name != entry.file.name }
    }

    fun readEntryText(entry: DiaryEntry): String {
        return entry.file.takeIf { it.exists() }?.readText().orEmpty()
    }

    private fun loadEntriesOnce(): List<DiaryEntry> {
        val files = getApplication<Application>().filesDir.listFiles().orEmpty()

        return files
            .filter { it.isFile && it.extension.equals("txt", ignoreCase = true) }
            .mapNotNull { file ->
                val nameWithoutExt = file.nameWithoutExtension
                val underscoreIndex = nameWithoutExt.indexOf('_')
                if (underscoreIndex <= 0) return@mapNotNull null

                val timestamp = nameWithoutExt.substring(0, underscoreIndex).toLongOrNull()
                    ?: return@mapNotNull null
                val fileTitlePart = nameWithoutExt.substring(underscoreIndex + 1)
                val title = if (fileTitlePart == DEFAULT_FILE_TITLE) {
                    ""
                } else {
                    fileTitlePart.replace('_', ' ')
                }
                val preview = file.readText().trim().take(40)

                DiaryEntry(
                    file = file,
                    timestamp = timestamp,
                    title = title,
                    preview = preview
                )
            }
            .sortedByDescending { it.timestamp }
    }

    private fun sanitizeTitleForFile(title: String): String {
        if (title.isBlank()) return DEFAULT_FILE_TITLE
        val safe = title.replace("\\s+".toRegex(), "_")
            .replace(Regex("[^A-Za-zА-Яа-я0-9_\\-]"), "")
            .trim('_')
        return if (safe.isBlank()) DEFAULT_FILE_TITLE else safe
    }

    companion object {
        private const val DEFAULT_FILE_TITLE = "Без_заголовка"
    }
}
