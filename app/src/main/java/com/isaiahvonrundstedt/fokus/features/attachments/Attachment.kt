package com.isaiahvonrundstedt.fokus.features.attachments

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.Okio
import java.io.File
import java.time.ZonedDateTime
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
    var name: String? = null,
    var target: String? = null,
    var task: String = "",
    var type: Int = 0,
    @TypeConverters(DateTimeConverter::class)
    var dateAttached: ZonedDateTime? = ZonedDateTime.now()
) : Parcelable {

    @DrawableRes
    fun getIconResource(): Int {
        return when(type) {
            TYPE_WEBSITE_LINK -> R.drawable.ic_hero_link_24
            else -> R.drawable.ic_hero_document_24
        }
    }

    companion object {
        const val TYPE_UNKNOWN = 0
        const val TYPE_CONTENT_URI = 1
        const val TYPE_IMPORTED_FILE = 2
        const val TYPE_WEBSITE_LINK = 3

        fun toJsonFile(items: List<Attachment>, directory: File,
                       name: String = Streamable.FILE_NAME_ATTACHMENT): File {
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