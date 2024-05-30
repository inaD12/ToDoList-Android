package com.example.to_dolist.data.entities

data class TaskDBEntity(
    var name: String = "",
    var desc: String = "",
    var dueTime: String? = null,
    var completedDate: String? = null,
    var id: String? = null
) {

    companion object {
        fun fromTaskItem(taskItem: TaskItem): TaskDBEntity {
            return TaskDBEntity(
                taskItem.name,
                taskItem.desc,
                taskItem.dueTime?.toString(),
                taskItem.completedDate?.toString(),
                taskItem.id
            )
        }
    }
}