package com.florian_walther.todo

import androidx.appcompat.widget.SearchView

// define new extension method for class SearchView which takes a lambda as argument
// this method is used in TasksFragment.onCreateOptionsMenu()
// 'inline' keyword makes the function efficient
// 'crossinline' keyword allows the lambda to be called within an anonymous class method
inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    setOnQueryTextListener(object: SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            // don't care for this method
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            // process each character being typed
            listener(newText.orEmpty())
            return true
        }
    })
}