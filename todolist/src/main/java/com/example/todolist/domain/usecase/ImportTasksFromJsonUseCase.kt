package com.example.todolist.domain.usecase

import com.example.todolist.data.preferences.TodoPreferencesRepository
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first

class ImportTasksFromJsonUseCase(
    private val repository: TodoRepository,
    private val preferencesRepository: TodoPreferencesRepository
) {
    suspend operator fun invoke() {
        val alreadyImported = preferencesRepository.isJsonImported.first()
        val hasData = repository.getTaskCount() > 0
        if (alreadyImported || hasData) return

        val tasks = repository.readInitialTasksFromJson()
        repository.importTasks(tasks)
        preferencesRepository.setJsonImported()
    }
}
