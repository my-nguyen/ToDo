package com.florian_walther.todo

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName="tasks")
@Parcelize
data class Task(val name: String, val is_important: Boolean=false, val is_completed: Boolean=false,
                val date: Long=System.currentTimeMillis(), @PrimaryKey(autoGenerate=true) val id: Int=0
                ): Parcelable {
    val formattedDate: String
        get() = DateFormat.getDateTimeInstance().format(date)
}
