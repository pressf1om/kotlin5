package com.example.d

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class Screen {
    object List : Screen()
    data class Editor(val entry: DiaryEntry?) : Screen()
}

@Composable
fun DiaryApp(diaryViewModel: DiaryViewModel = viewModel()) {
    val entries by diaryViewModel.entries.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    when (val screen = currentScreen) {
        Screen.List -> {
            DiaryListScreen(
                entries = entries,
                onAddClick = { currentScreen = Screen.Editor(entry = null) },
                onEntryClick = { entry -> currentScreen = Screen.Editor(entry = entry) },
                onDeleteClick = diaryViewModel::deleteEntry
            )
        }

        is Screen.Editor -> {
            DiaryEditorScreen(
                initialEntry = screen.entry,
                readText = { entry -> diaryViewModel.readEntryText(entry) },
                onBack = { currentScreen = Screen.List },
                onSave = { title, text ->
                    diaryViewModel.saveEntry(title = title, text = text, existingEntry = screen.entry)
                    currentScreen = Screen.List
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryListScreen(
    entries: List<DiaryEntry>,
    onAddClick: () -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    onDeleteClick: (DiaryEntry) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Дневник") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Новая запись")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Новая запись")
                }
            }
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "У вас пока нет записей",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Нажмите +, чтобы создать первую",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(entries, key = { it.file.name }) { entry ->
                    DiaryListItem(
                        entry = entry,
                        onClick = { onEntryClick(entry) },
                        onDelete = { onDeleteClick(entry) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiaryListItem(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = if (entry.title.isBlank()) "Без заголовка" else entry.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.preview.ifBlank { "(Пустая запись)" },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatDate(entry.timestamp),
            style = MaterialTheme.typography.bodySmall
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = {
                    menuExpanded = false
                    onDelete()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryEditorScreen(
    initialEntry: DiaryEntry?,
    readText: (DiaryEntry) -> String,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by rememberSaveable(initialEntry?.file?.name ?: "new") {
        mutableStateOf(initialEntry?.title.orEmpty())
    }
    var text by rememberSaveable(initialEntry?.file?.name ?: "new-text") {
        mutableStateOf("")
    }

    LaunchedEffect(initialEntry?.file?.name) {
        text = if (initialEntry != null) readText(initialEntry) else ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialEntry == null) "Новая запись" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSave(title, text) },
                        enabled = text.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Заголовок (необязательно)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Текст записи") }
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
