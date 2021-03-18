package com.florian_walther.todo

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel

class TaskViewModel @ViewModelInject constructor(private val taskDao: TaskDao): ViewModel() {
}