package com.florian_walther.todo

// an extension property, similar to extension function, of any type T which returns the same object;
// its purpose is to turn a statement into an expression. this is used in TasksFragment.onViewCreated
// in the block viewModel.taskEvent.collect, at the end of the when statement, to add 2 more branches
// for the 2 newly created events TaskViewModel.TaskEvent.NavigateToAdd and TaskViewModel.TaskEvent.NavigateToEdit
val <T> T.exhaustive: T
    get() = this