package com.example.to_dolist.data.recyclerview

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.to_dolist.data.entities.TaskItem
import com.example.to_dolist.data.interfaces.ITaskItemClickListener
import com.example.to_dolist.databinding.TaskRvRowBinding

class TaskAdapter(
    private val taskItems : List<TaskItem>,
    private val clickListener: ITaskItemClickListener
): RecyclerView.Adapter<TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = TaskRvRowBinding.inflate(from, parent, false)
        return TaskViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int = taskItems.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bindTaskItem((taskItems[position]))
    }

}