package com.florian_walther.todo

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.florian_walther.todo.databinding.FragmentTasksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment: Fragment(R.layout.fragment_tasks) {
    private val viewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val taskAdapter = TaskAdapter()
        val binding = FragmentTasksBinding.bind(view)
        binding.apply {
            rvTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.onQueryTextChanged { text ->
            // update viewModel.query here
            viewModel.query.value = text
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                true
            }
            R.id.action_sort_by_date -> {
                true
            }
            R.id.action_hide_completed -> {
                item.isChecked = !item.isChecked
                true
            }
            R.id.action_delete_completed -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}