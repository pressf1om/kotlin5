package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(private val repository: TodoRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getTasks()
}
