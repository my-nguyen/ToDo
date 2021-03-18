package com.florian_walther.todo

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities=[Task::class], version=1)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao

    // with Provider<>, TaskDatabase is instantiated lazily, meaning it will not be instantiated
    // when it is injected into the Callback constructor, but only later when database.get() is
    // executed inside onCreate(), which occurs after AppModule.provideDatabase() has been called.
    // this avoids a circular dependency problem: TaskDatabase depends on Callback which in turn
    // depends on TaskDatabase.
    class Callback @Inject constructor(private val database: Provider<TaskDatabase>,
                                       @ApplicationScope private val applicationScope: CoroutineScope
                                       ): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // TaskDatabase is only instantiated here
            val dao = database.get().taskDao()
            // db operations
            applicationScope.launch {
                dao.insert(Task("Wash the dishes"))
                dao.insert(Task("Do the laundry"))
                dao.insert(Task("Buy groceries", isImportant=true))
                dao.insert(Task("Prepare food", isCompleted=true))
                dao.insert(Task("Call mom"))
                dao.insert(Task("Visit grandma", isCompleted=true))
                dao.insert(Task("Repair my bike"))
                dao.insert(Task("Call Elon Musk"))
            }
        }
    }
}