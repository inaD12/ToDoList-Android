package com.example.to_dolist.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.to_dolist.data.entities.TaskDBEntity
import com.example.to_dolist.data.entities.TaskItem
import com.example.to_dolist.data.interfaces.ITaskRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskRepository(private val database: FirebaseDatabase, private val userId: String) : ITaskRepository {

    private val tasksReference: DatabaseReference
        get() = database.reference.child("Tasks").child(userId)

    override fun addTask(task: TaskItem) {
        val taskId = task.id ?: return
        val databaseRef = tasksReference.child(taskId)
        val taskDBEntity = TaskDBEntity.fromTaskItem(task)

        databaseRef.setValue(taskDBEntity)
            .addOnSuccessListener {
                Log.i("TaskRepository", "Task saved successfully!")
            }
            .addOnFailureListener { exception ->
                Log.e("TaskRepository", "Error saving task: ${exception.message}", exception)
            }
    }

    override fun deleteTask(taskId: String?) {
        val databaseRef = taskId?.let { tasksReference.child(it) }

        databaseRef?.removeValue()?.addOnSuccessListener {
            Log.i("TaskRepository", "Task deleted successfully!")
        }?.addOnFailureListener { exception ->
            Log.e("TaskRepository", "Error deleting task: ${exception.message}", exception)
        }
    }

    override fun getTasks(onTasksReceived: (List<TaskItem>) -> Unit) {
        tasksReference.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val taskList = mutableListOf<TaskItem>()
                snapshot.children.forEach { taskSnapshot ->
                    val taskDBEntity = taskSnapshot.getValue(TaskDBEntity::class.java)
                    taskDBEntity?.let {
                        val taskItem = TaskItem.fromTaskEntity(it)
                        taskList.add(taskItem)
                    }
                }
                onTasksReceived(taskList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TaskRepository", "Error fetching tasks", error.toException())
            }
        })
    }

    override fun updateTask(task: TaskItem) {
        val taskId = task.id ?: return
        val databaseRef = tasksReference.child(taskId)
        val taskDBEntity = TaskDBEntity.fromTaskItem(task)

        databaseRef.setValue(taskDBEntity)
            .addOnSuccessListener {
                Log.i("TaskRepository", "Task updated successfully!")
            }
            .addOnFailureListener { exception ->
                Log.e("TaskRepository", "Error updating task: ${exception.message}", exception)
            }
    }
}
