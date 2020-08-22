package com.isaiahvonrundstedt.fokus.features.log

import android.os.Parcelable
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.Okio
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.io.InputStream
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "logs")
data class Log @JvmOverloads constructor(
    @PrimaryKey
    var logID: String = UUID.randomUUID().toString(),
    var title: String? = null,
    var content: String? = null,
    var data: String? = null,
    var type: Int = TYPE_GENERIC,
    var isImportant: Boolean = false,
    @TypeConverters(DateTimeConverter::class)
    var dateTimeTriggered: DateTime? = null
) : Parcelable, Streamable {

    fun setIconToView(sourceView: ImageView) {
        with(sourceView) {
            background = ContextCompat.getDrawable(this.context,
                R.drawable.shape_background_icon)?.also {
                it.setTint(ContextCompat.getColor(this.context, getIconBackgroundColorResource()))
            }
            setImageDrawable(ContextCompat.getDrawable(this.context, getIconResource())?.also {
                it.setTint(ContextCompat.getColor(this.context, getIconColorResource()))
            })
        }
    }

    fun formatDateTime(): String {
        val currentDateTime = LocalDate.now()

        // Formats the dateTime object for human reading
        return if (dateTimeTriggered!!.toLocalDate().isEqual(currentDateTime))
            DateTimeFormat.forPattern(DateTimeConverter.FORMAT_TIME).print(dateTimeTriggered)
        else if (dateTimeTriggered!!.toLocalDate().year == currentDateTime.year)
            DateTimeFormat.forPattern("MMMM d").print(dateTimeTriggered!!)
        else DateTimeFormat.forPattern("MMMM d yyyy").print(dateTimeTriggered!!)
    }

    @DrawableRes
    fun getIconResource(): Int {
        return when (type) {
            TYPE_TASK -> R.drawable.ic_hero_check_24
            TYPE_EVENT -> R.drawable.ic_hero_calendar_24
            TYPE_CLASS -> R.drawable.ic_hero_beaker_24
            TYPE_GENERIC -> R.drawable.ic_hero_light_bulb_24
            else -> R.drawable.ic_hero_light_bulb_24
        }
    }

    @ColorRes
    private fun getIconColorResource(): Int {
        return when (type) {
            TYPE_TASK -> R.color.color_theme_blue
            TYPE_EVENT -> R.color.color_theme_amber
            TYPE_CLASS -> R.color.color_theme_red
            TYPE_GENERIC -> R.color.color_theme_teal
            else -> R.color.color_theme_teal
        }
    }

    @ColorRes
    private fun getIconBackgroundColorResource(): Int {
        return when (type) {
            TYPE_TASK -> R.color.color_theme_blue_variant
            TYPE_EVENT -> R.color.color_theme_amber_variant
            TYPE_CLASS -> R.color.color_theme_red_variant
            TYPE_GENERIC -> R.color.color_theme_teal_variant
            else -> R.color.color_theme_teal_variant
        }
    }

    override fun toJson(): String? = JsonDataStreamer.encodeToJson(this, Log::class.java)

    override fun writeToFile(destination: File, name: String): File {
        return File(destination, name).apply {
            Okio.buffer(Okio.sink(this)).use {
                toJson()?.also { json -> it.write(json.toByteArray()) }
                it.flush()
            }
        }
    }

    override fun parseInputStream(inputStream: InputStream) {
        JsonDataStreamer.decodeOnceFromJson(inputStream, Log::class.java)?.also {
            logID = it.logID
            title = it.title
            content = it.content
            type = it.type
            isImportant = it.isImportant
            data = it.data
            dateTimeTriggered = it.dateTimeTriggered
        }
    }

    companion object {
        const val TYPE_GENERIC = 0
        const val TYPE_TASK = 1
        const val TYPE_EVENT = 2
        const val TYPE_CLASS = 3
    }
}