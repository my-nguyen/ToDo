package com.florian_walther.todo

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// SavedStateHandle is like a Bundle in Activity.onCreate() or Activity.recreate() where we can
// restore data after a process death.
class EditViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // SavedStateHandle also saves any Navigation argument sent to the current Fragment.
    // here we retrieve the Task argument from SavedStateHandle; the key "task" must match the
    // argument name passed in Navigation graph
    val task = state.get<Task>("task")

    // retrieve taskName if saved earlier; if it didn't get saved, then take task.name, and if task
    // is null then set taskName to empty string
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            // as soon as taskName is updated, save it in SavedStateHandle
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.is_important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    // channel to emit events to Fragment
    private val editChannel = Channel<EditEvent>()
    // turn channel to Flow to avoid exposure of channel
    val editEvent = editChannel.receiveAsFlow()

    fun onSaveClick() {
        when {
            taskName.isBlank() -> {
                showInvalidInput("Name cannot be empty")
            }
            task != null -> {
                val updatedTask = task.copy(name=taskName, is_important=taskImportance)
                updateTask(updatedTask)
            }
            else -> {
                val newTask = Task(name=taskName, is_important=taskImportance)
                createTask(newTask)
            }
        }
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        // navigate back
        editChannel.send(EditEvent.NavigateBack(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        // navigate back
        editChannel.send(EditEvent.NavigateBack(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInput(text: String) = viewModelScope.launch {
        editChannel.send(EditEvent.ShowInvalidInput(text))
    }

    sealed class EditEvent {
        data class ShowInvalidInput(val message: String): EditEvent()
        data class NavigateBack(val result: Int): EditEvent()
    }
}