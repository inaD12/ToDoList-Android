package com.example.to_dolist.data.entities

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.example.to_dolist.R
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class TaskItem(
    var name: String,
    var desc: String,
    var dueTime: LocalTime?,
    var completedDate: LocalDate?,
    var id: String? = UUID.randomUUID().toString()
) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readSerializable() as? LocalTime?,
        parcel.readSerializable() as? LocalDate?,
        parcel.readString()
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(desc)
        parcel.writeSerializable(dueTime)
        parcel.writeSerializable(completedDate)
        parcel.writeString(id)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TaskItem> {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun createFromParcel(parcel: Parcel): TaskItem = TaskItem(parcel)
        override fun newArray(size: Int): Array<TaskItem?> = arrayOfNulls(size)

        @RequiresApi(Build.VERSION_CODES.O)
        fun fromTaskEntity(taskEntity: TaskDBEntity): TaskItem {
            return TaskItem(
                taskEntity.name,
                taskEntity.desc,
                taskEntity.dueTime?.let { LocalTime.parse(it) },
                taskEntity.completedDate?.let { LocalDate.parse(it) },
                taskEntity.id
            )
        }
    }

    fun isCompleted(): Boolean = completedDate != null

    fun imageResource(): Int = if (isCompleted()) R.drawable.check_24 else R.drawable.uncheck_24
}