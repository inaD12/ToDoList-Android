package com.example.to_dolist.data.recyclerview

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.to_dolist.data.entities.TaskItem
import com.example.to_dolist.data.interfaces.ITaskItemClickListener
import com.example.to_dolist.databinding.TaskRvRowBinding
import java.time.format.DateTimeFormatter

class TaskViewHolder(
    private val binding: TaskRvRowBinding,
    private val clickListener: ITaskItemClickListener
) : RecyclerView.ViewHolder(binding.root) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    fun bindTaskItem(taskItem: TaskItem) {
        binding.apply {
            name.text = taskItem.name

            desc.text = taskItem.desc

            if (taskItem.isCompleted()) {
                name.paintFlags = name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                dueTime.paintFlags = dueTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                name.paintFlags = name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                dueTime.paintFlags = dueTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            completeButton.setImageResource(taskItem.imageResource())

            completeButton.setOnClickListener {
                clickListener.completeTaskItem(taskItem)
            }
            taskCellContainer.setOnClickListener {
                clickListener.editTaskItem(taskItem)
            }

            dueTime.text = taskItem.dueTime?.format(timeFormat) ?: ""
        }
    }
}