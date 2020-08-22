package com.isaiahvonrundstedt.fokus.features.attachments

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.Okio
import org.joda.time.DateTime
import java.io.File
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
    var target: String? = null,
    var task: String = "",
    @TypeConverters(DateTimeConverter::class)
    var dateAttached: DateTime? = DateTime.now()
) : Parcelable {

    companion object {

        fun writeToFile(items: List<Attachment>, directory: File, name: String = Streamable.FILE_NAME_ATTACHMENT): File {
            return File(directory, name).apply {
                Okio.buffer(Okio.sink(this)).use {
                    JsonDataStreamer.encodeToJson(items, Attachment::class.java)?.also { json ->
                        it.write(json.toByteArray())
                    }
                }
            }
        }
    }
}