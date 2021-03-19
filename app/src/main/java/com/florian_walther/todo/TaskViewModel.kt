package com.florian_walther.todo

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferences: PreferenceRepository
) : ViewModel() {
    // query to be updated in TasksFragment.onCreateOptionsMenu()
    val query = MutableStateFlow("")

    /// when flatMapLatest receives 1 single flow (query)
    // query is a flow, and flatMapLatest is a flow operator. whenever the value of the query flow
    // changes (when user types in the SearchView), flatMapLatest executes a lambda, passing the new
    // value as 'it' into the lambda. here the lambda calls taskDao.getTasks(it) which runs 'it' as
    // a SQLite query and returns a flow, which is assigned to taskFlow.
    /*private val taskFlow = query.flatMapLatest {
        taskDao.getTasks(it)
    }*/

    /// when flatMapLatest receives 3 flows (query, sortOrder, hideCompleted)
    /*val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    val hideCompleted = MutableStateFlow(false)
    // each argument to combine() (query, sortOrder, hideCompleted) is a flow. whenever any flow
    // emits a new value, combine() will execute the lambda with the latest value of all 3 flows.
    private val taskFlow = combine(query, sortOrder, hideCompleted) { query, sortOrder, hideCompleted ->
        // Triple is used to pack the 3 flows into 1 single argument to pass into flatMapLatest
        Triple(query, sortOrder, hideCompleted)
    }.flatMapLatest { (query, sortOrder, hideCompleted) ->
        // the 'it' Triple argument is destructured/unpacked back into the 3 flows
        taskDao.getTasks(query, sortOrder, hideCompleted)
    }*/

    /// when flatMapLatest receives 2 flows (query, preferencesFlow)
    val preferencesFlow = preferences.preferencesFlow
    private val taskFlow = combine(query, preferencesFlow) { query, preferencesFlow ->
        // Pair is used to pack the 2 flows into 1 single argument to pass into flatMapLatest
        Pair(query, preferencesFlow)
    }.flatMapLatest { (query, preferencesFlow) ->
        // the 'it' Pair argument is destructured/unpacked back into the 2 flows
        taskDao.getTasks(query, preferencesFlow.sortOrder, preferencesFlow.hideCompleted)
    }

    // the query result taskFlow is observed as a LiveData
    val tasks = taskFlow.asLiveData()

    private val taskChannel = Channel<TaskEvent>()
    // to avoid exposing taskChannel, turn taskChannel into a flow so Fragment can receive from taskChannel
    val taskEvent = taskChannel.receiveAsFlow()

    // this viewModelScope is alive as long as ViewModel is alive
    // as opposed to ApplicationScope is alive as long as the application is alive
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferences.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClicked(hideCompleted: Boolean) = viewModelScope.launch {
        preferences.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) {
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        val copy = task.copy(is_completed=isChecked)
        taskDao.update(copy)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)

        // Snackbar is tricky: the ViewModel contains logic of when to show a Snackbar, and the
        // Fragment has all the ingredients necessary to show the Snackbar, but the ViewModel
        // doesn't maintain a reference to the Fragment
        // solution: use a Kotlin channel
        val element = TaskEvent.ShowUndoMessage(task)
        taskChannel.send(element)
    }

    fun onUndoClick(task: Task) = viewModelScope.launch {
        // undo: insert task back into database
        taskDao.insert(task)
    }

    sealed class TaskEvent {
        data class ShowUndoMessage(val task: Task): TaskEvent()
    }
}
