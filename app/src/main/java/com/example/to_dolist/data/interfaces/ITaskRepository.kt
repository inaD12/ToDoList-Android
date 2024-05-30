package com.example.to_dolist.data.interfaces

import com.example.to_dolist.data.entities.TaskItem

interface ITaskRepository {
    fun addTask(task: TaskItem)

    fun deleteTask(taskId: String?)

    fun getTasks(
        onTasksReceived: (List<TaskItem>) -> Unit
    )

    fun updateTask(
        task: TaskItem,
    )
}