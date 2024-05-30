package com.example.to_dolist

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.to_dolist.data.SpeechToTextContract
import com.example.to_dolist.data.TaskRepository
import com.example.to_dolist.data.TaskViewModel
import com.example.to_dolist.data.entities.TaskItem
import com.example.to_dolist.data.factories.TaskViewModelFactory
import com.example.to_dolist.data.interfaces.ITaskRepository
import com.example.to_dolist.databinding.FragmentNewTaskSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalTime


class NewTaskSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentNewTaskSheetBinding
    private lateinit var taskViewModel: TaskViewModel
    private var taskItem: TaskItem? = null
    private var dueTime: LocalTime? = null
    private lateinit var repository: ITaskRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: FirebaseDatabase
    private lateinit var authId: String

    private lateinit var speechToTextLauncherForName: ActivityResultLauncher<Unit>
    private lateinit var speechToTextLauncherForDesc: ActivityResultLauncher<Unit>

    companion object {
        private const val ARG_TASK_ITEM = "task_item"

        fun newInstance(taskItem: TaskItem?): NewTaskSheet {
            return NewTaskSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK_ITEM, taskItem)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            manageHeight(dialogInterface as BottomSheetDialog)
        }
        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupUI()
        setupListeners()
    }

    private fun manageHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams

            layoutParams.height = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                ViewGroup.LayoutParams.MATCH_PARENT
            else
                ViewGroup.LayoutParams.WRAP_CONTENT

            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskItem = arguments?.getParcelable(ARG_TASK_ITEM)
        initSpeechToTextLaunchers()
    }

    private fun initSpeechToTextLaunchers() {
        speechToTextLauncherForName = registerForActivityResult(SpeechToTextContract()) { spokenText ->
            binding.name.text = spokenText?.let { Editable.Factory.getInstance().newEditable(it) } ?: run {
                Toast.makeText(context, "Speech recognition failed", Toast.LENGTH_SHORT).show()
                null
            }
        }

        speechToTextLauncherForDesc = registerForActivityResult(SpeechToTextContract()) { spokenText ->
            binding.desc.text = spokenText?.let { Editable.Factory.getInstance().newEditable(it) } ?: run {
                Toast.makeText(context, "Speech recognition failed", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        binding.taskTitle.text = if (taskItem == null) "New Task" else "Edit Task"
        if (taskItem == null) {
            binding.trashIcon.visibility = View.GONE
        } else {
            binding.trashIcon.visibility = View.VISIBLE
            taskItem?.let {
                binding.name.text = Editable.Factory.getInstance().newEditable(it.name)
                binding.desc.text = Editable.Factory.getInstance().newEditable(it.desc)
                it.dueTime?.let { dueTime -> updateTimeButtonText(dueTime) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.saveButton.setOnClickListener { saveAction() }
        binding.timePickerButton.setOnClickListener { openTimePicker() }

        binding.nameLayout.setEndIconOnClickListener {
            speechToTextLauncherForName.launch(Unit)
        }

        binding.descLayout.setEndIconOnClickListener {
            speechToTextLauncherForDesc.launch(Unit)
        }

        binding.trashIcon.setOnClickListener{
            taskViewModel.deleteTaskItem(taskItem?.id)
            dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openTimePicker() {
        val currentDueTime = taskItem?.dueTime ?: LocalTime.now()
        val listener = TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            val selectedTime = LocalTime.of(selectedHour, selectedMinute)
            dueTime = selectedTime
            taskItem?.dueTime = selectedTime
            updateTimeButtonText(selectedTime)
        }
        TimePickerDialog(activity, listener, currentDueTime.hour, currentDueTime.minute, true).apply {
            setTitle("Task Due")
            show()
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTimeButtonText(time: LocalTime) {
        binding.timePickerButton.text = String.format("%02d:%02d", time.hour, time.minute)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewTaskSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun saveAction() {
        val name = binding.name.text.toString()
        val desc = binding.desc.text.toString()

        if (taskItem == null) {
            taskViewModel.addTaskItem(TaskItem(name, desc, dueTime, null))
        } else {
            taskItem?.apply {
                this.name = name
                this.desc = desc
                id?.let { taskViewModel.updateTaskItem(it, name, desc, dueTime) }
            }
        }

        binding.name.setText("")
        binding.desc.setText("")
        dismiss()
    }

    private fun initViewModel() {
        auth = FirebaseAuth.getInstance()
        auth.currentUser?.let {
            authId = it.uid
            databaseRef = FirebaseDatabase.getInstance("https://to-do-list-30c11-default-rtdb.europe-west1.firebasedatabase.app")
            repository = TaskRepository(databaseRef, authId)

            val factory = TaskViewModelFactory(repository)
            taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
        } ?: run {
            dismiss()
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
