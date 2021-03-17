package com.florian_walther.todo

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity
@Parcelize
data class Task(val name: String, val isImportant: Boolean=false, val isCompleted: Boolean=false,
                val date: Long=System.currentTimeMillis(), @PrimaryKey(autoGenerate=true) val id: Int=0
                ): Parcelable {
    val formattedDate: String
        get() = DateFormat.getDateTimeInstance().format(date)
}
