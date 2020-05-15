package com.isaiahvonrundstedt.fokus.features.attachments

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.UriConverter
import com.isaiahvonrundstedt.fokus.features.task.Task
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.*

@Entity(tableName = "attachments", foreignKeys = [ForeignKey(entity = Task::class,
    parentColumns = arrayOf("taskID"), childColumns = arrayOf("taskID"),
    onDelete = ForeignKey.CASCADE)
])
data class Attachment @JvmOverloads constructor (
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    @TypeConverters(UriConverter::class)
    var uri: Uri? = null,
    var taskID: String = "",
    @TypeConverters(DateTimeConverter::class)
    var dateAttached: LocalDateTime? = LocalDateTime.now()
)