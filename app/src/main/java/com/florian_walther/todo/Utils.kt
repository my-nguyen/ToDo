package com.florian_walther.todo

// this is an extension property, similar to extension function, of any type T; the property returns
// the same object. its purpose is to turn a statement into an expression. it is used to add all
// exhaustive branches into a when statement
val <T> T.exhaustive: T
    get() = this