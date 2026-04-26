package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TodoRepository

class AddTaskUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(task: Task) = repository.addTask(task)
}
