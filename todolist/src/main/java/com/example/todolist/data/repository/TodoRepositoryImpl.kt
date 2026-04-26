package com.example.todolist.data.repository

import android.content.Context
import com.example.todolist.data.local.TaskEntity
import com.example.todolist.data.local.TodoDao
import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class TodoRepositoryImpl(
    private val dao: TodoDao,
    private val context: Context
) : TodoRepository {

    override fun getTasks(): Flow<List<Task>> {
        return dao.getTasks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addTask(task: Task) {
        dao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task.toEntity())
    }

    override suspend fun getTaskCount(): Int = dao.getTaskCount()

    override suspend fun importTasks(tasks: List<Task>) {
        tasks.forEach { dao.insertTask(it.toEntity()) }
    }

    override fun readInitialTasksFromJson(): List<Task> {
        val raw = context.assets.open("tasks.json").bufferedReader().use { it.readText() }
        val array = JSONArray(raw)
        val result = mutableListOf<Task>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                Task(
                    title = obj.optString("title"),
                    description = obj.optString("description"),
                    isDone = obj.optBoolean("isDone", false)
                )
            )
        }
        return result
    }
}

private fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        isDone = isDone
    )
}

private fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        isDone = isDone
    )
}
