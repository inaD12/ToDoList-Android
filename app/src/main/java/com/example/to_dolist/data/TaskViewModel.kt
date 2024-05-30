package com.example.to_dolist.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.to_dolist.data.entities.TaskItem
import com.example.to_dolist.data.interfaces.ITaskRepository
import java.time.LocalDate
import java.time.LocalTime

class TaskViewModel(private val taskRepository: ITaskRepository) : ViewModel() {
    val taskItems: MutableLiveData<List<TaskItem>> = MutableLiveData()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        taskRepository.getTasks { tasks ->
            taskItems.value = tasks
        }
    }

    fun addTaskItem(newTask: TaskItem) {
        taskRepository.addTask(newTask)
    }

    fun deleteTaskItem(taskId: String?){
        taskRepository.deleteTask(taskId)
    }

    fun updateTaskItem(id: String, name: String, desc: String, dueTime: LocalTime?) {
        taskItems.value?.let { list ->
            val taskIndex = list.indexOfFirst { it.id == id }
            if (taskIndex != -1) {
                val updatedTask = list[taskIndex].copy(name = name, desc = desc, dueTime = dueTime)

                taskRepository.updateTask(updatedTask)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCompleted(taskItem: TaskItem) {
        if (taskItem.completedDate == null) {
            val updatedTask = taskItem.copy(completedDate = LocalDate.now())
            taskRepository.updateTask(updatedTask)
        }
    }
}
