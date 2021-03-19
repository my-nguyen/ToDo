package com.florian_walther.todo

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.florian_walther.todo.databinding.FragmentTasksBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment: Fragment(R.layout.fragment_tasks), TaskAdapter.OnItemClickListener {
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val taskAdapter = TaskAdapter(this)
        val binding = FragmentTasksBinding.bind(view)
        binding.apply {
            rvTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            val swipeDirections = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, swipeDirections) {
                // onMove is motion up and down, not what we want
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                // remove a task when user swipes left or right
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val task = taskAdapter.currentList[position]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(rvTasks)

            fabAddTask.setOnClickListener {
                viewModel.onAddTaskClick()
            }
        }

        // receive result from EditFragment
        setFragmentResultListener("edit_request") { _, bundle ->
            val result = bundle.getInt("edit_result")
            viewModel.onEditResult(result)
        }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
        }

        // launchWhenStarted() prevents Fragment from showing the Snackbar when Fragment is obscured
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // listen for event from channel
            viewModel.taskEvent.collect { event ->
                when (event) {
                    is TaskViewModel.TaskEvent.ShowUndoMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                // Fragment is dumb so it delegates the undo logic back to ViewModel
                                viewModel.onUndoClick(event.task)
                            }.show()
                    }
                    is TaskViewModel.TaskEvent.NavigateToAdd -> {
                        // must re-build for compile to generate navigation methods
                        val action = TasksFragmentDirections.actionTasksFragmentToEditFragment(null, "New Task")
                        findNavController().navigate(action)
                        // the above could be written as below but if the destination Fragment
                        // expects an argument as above, then the above would cause a compile error
                        // whereas the below would not.
                        // findNavController().navigate(R.id.tasksFragment)
                    }
                    is TaskViewModel.TaskEvent.NavigateToEdit -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToEditFragment(event.task, "Edit Task")
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.ShowTaskSavedMessage -> {
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is TaskViewModel.TaskEvent.NavigateToDeleteCompleted -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialog()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }

    // in the following 2 onClick methods, we delegate the logic to ViewModel instead of implementing
    // it here.
    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        // fix for a bug where after entering a query thereby executing it, the screen is rotated:
        // the searchView is contracted and the title shows instead
        // retrieve the current query from ViewModel if any
        val pendingQuery = viewModel.query.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            // expand the magnifying glass
            searchItem.expandActionView()
            // restore the query string
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged { text ->
            // update viewModel.query here
            viewModel.query.value = text
        }

        // read hideCompleted value from preferencesFlow and set its value on the corresponding menu item
        viewLifecycleOwner.lifecycleScope.launch {
            val actionHideCompleted = menu.findItem(R.id.action_hide_completed)
            actionHideCompleted.isChecked = viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // note each menu action from the UI triggers a ViewModel action, so the UI doesn't directly
        // manipulate data and leaves it up to the ViewModel
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                // viewModel.sortOrder.value = SortOrder.BY_NAME
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date -> {
                // viewModel.sortOrder.value = SortOrder.BY_DATE
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed -> {
                item.isChecked = !item.isChecked
                // viewModel.hideCompleted.value = item.isChecked
                viewModel.onHideCompletedClicked(item.isChecked)
                true
            }
            R.id.action_delete_completed -> {
                viewModel.onDeleteCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // remove the listener from searchView
        searchView.setOnQueryTextListener(null)
    }
}