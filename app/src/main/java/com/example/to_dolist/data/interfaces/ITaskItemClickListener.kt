package com.example.to_dolist.data.interfaces

import com.example.to_dolist.data.entities.TaskItem

interface ITaskItemClickListener {
    fun editTaskItem(taskItem: TaskItem)
    fun completeTaskItem(taskItem: TaskItem)
}