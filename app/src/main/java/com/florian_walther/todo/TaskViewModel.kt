package com.florian_walther.todo

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class TaskViewModel @ViewModelInject constructor(private val taskDao: TaskDao): ViewModel() {
    val tasks = taskDao.getTasks().asLiveData()
}