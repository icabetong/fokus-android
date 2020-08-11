package com.isaiahvonrundstedt.fokus.features.attachments

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.database.converter.UriConverter
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "attachments", foreignKeys = [ForeignKey(entity = Task::class,
    parentColumns = arrayOf("taskID"), childColumns = arrayOf("task"),
    onDelete = ForeignKey.CASCADE)
])
data class Attachment @JvmOverloads constructor(
    @PrimaryKey
    var attachmentID: String = UUID.randomUUID().toString(),
    @TypeConverters(UriConverter::class)
    var uri: Uri? = null,
    var task: String = "",
    @TypeConverters(DateTimeConverter::class)
    var dateAttached: DateTime? = DateTime.now()
) : Parcelable