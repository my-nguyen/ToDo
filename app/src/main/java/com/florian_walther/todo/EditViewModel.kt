package com.florian_walther.todo

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

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
}