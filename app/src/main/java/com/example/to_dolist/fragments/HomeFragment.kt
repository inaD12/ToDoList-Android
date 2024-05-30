package com.example.to_dolist.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.to_dolist.NewTaskSheet
import com.example.to_dolist.R
import com.example.to_dolist.data.TaskRepository
import com.example.to_dolist.data.TaskViewModel
import com.example.to_dolist.data.entities.TaskItem
import com.example.to_dolist.data.factories.TaskViewModelFactory
import com.example.to_dolist.data.interfaces.ITaskItemClickListener
import com.example.to_dolist.data.interfaces.ITaskRepository
import com.example.to_dolist.data.recyclerview.TaskAdapter
import com.example.to_dolist.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment(), ITaskItemClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var repository: ITaskRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: FirebaseDatabase
    private lateinit var authId: String
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        initFirebase()
        initViewModel()
        setupUI()

        setRecyclerView()
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        auth.currentUser?.let {
            authId = it.uid
            databaseRef = FirebaseDatabase.getInstance("https://to-do-list-30c11-default-rtdb.europe-west1.firebasedatabase.app")
        } ?: run {
            navController.navigate(R.id.action_homeFragment_to_signInFragment)
        }
    }

    private fun initViewModel() {
        repository = TaskRepository(databaseRef, authId)
        val factory = TaskViewModelFactory(repository)
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    private fun setupUI() {
        binding.newTaskButton.setOnClickListener {
            NewTaskSheet.newInstance(null).show(parentFragmentManager, "newTaskTag")
        }

        binding.loguotButton.setOnClickListener {
            auth.signOut()
            navController.navigate(R.id.action_homeFragment_to_signInFragment)
        }
    }

    private fun setRecyclerView() {
        binding.todoListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskViewModel.taskItems.observe(viewLifecycleOwner) { taskList ->
            binding.todoListRecyclerView.adapter = TaskAdapter(taskList, this@HomeFragment)
        }
    }

    override fun editTaskItem(taskItem: TaskItem) {
        NewTaskSheet.newInstance(taskItem).show(parentFragmentManager, "newTaskTag")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun completeTaskItem(taskItem: TaskItem) {
        taskViewModel.setCompleted(taskItem)
    }
}