package com.florian_walther.todo

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class TaskViewModel @ViewModelInject constructor(private val taskDao: TaskDao): ViewModel() {
    // query to be updated in TasksFragment.onCreateOptionsMenu()
    val query = MutableStateFlow("")
    // query is a flow, and flatMapLatest is a flow operator. whenever the value of the query flow
    // changes (when user types in the SearchView), flatMapLatest executes a lambda, passing the new
    // value as 'it' into the lambda. here the lambda calls taskDao.getTasks(it) which runs 'it' as
    // a SQLite query and returns a flow, which is assigned to taskFlow.
    private val taskFlow = query.flatMapLatest {
        taskDao.getTasks(it)
    }
    // the query result taskFlow is observed as a LiveData
    val tasks = taskFlow.asLiveData()
}