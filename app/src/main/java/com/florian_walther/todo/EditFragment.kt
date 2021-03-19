package com.florian_walther.todo

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.florian_walther.todo.databinding.FragmentEditBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class EditFragment: Fragment(R.layout.fragment_edit) {
    private val viewModel: EditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentEditBinding.bind(view)
        binding.apply {
            etTaskName.setText(viewModel.taskName)
            cbImportant.isChecked = viewModel.taskImportance
            // skip default animation to show checkbox
            cbImportant.jumpDrawablesToCurrentState()
            tvDateCreated.isVisible = viewModel.task != null
            tvDateCreated.text = "Created: ${viewModel.task?.formattedDate}"

            etTaskName.addTextChangedListener {
                // save new value of taskName to the ViewModel, including its corresponding SavedStateHandle
                viewModel.taskName = it.toString()
            }

            cbImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.editEvent.collect { event ->
                when (event) {
                    is EditViewModel.EditEvent.NavigateBack -> {
                        // hide the keyboard
                        binding.etTaskName.clearFocus()

                        // use of FragmentResult, a new API, to exit the current Fragment and return
                        // result to the previous Fragment
                        val bundle = bundleOf("edit_result" to event.result)
                        setFragmentResult("edit_request", bundle)
                        // remove the current Fragment from the back stack and go back to the previous Fragment
                        findNavController().popBackStack()
                    }
                    is EditViewModel.EditEvent.ShowInvalidInput -> {
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }
}