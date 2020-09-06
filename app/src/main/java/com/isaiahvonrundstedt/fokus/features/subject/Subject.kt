package com.isaiahvonrundstedt.fokus.features.subject

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.ColorConverter
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.Okio
import java.io.File
import java.io.InputStream
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "subjects")
data class Subject @JvmOverloads constructor(
    @PrimaryKey
    @ColumnInfo(index = true)
    var subjectID: String = UUID.randomUUID().toString(),
    var code: String? = null,
    var description: String? = null,
    @TypeConverters(ColorConverter::class)
    var tag: Tag = Tag.SKY
) : Parcelable, Streamable {

    // Used for the color tag of the subject
    enum class Tag(val color: Int) {
        SKY(Color.parseColor("#2196f3")),
        GRASS(Color.parseColor("#71a234")),
        SUNSET(Color.parseColor("#ff7e0f")),
        LEMON(Color.parseColor("#ffb600")),
        SEA(Color.parseColor("#01b1af")),
        GRAPE(Color.parseColor("#9c27b0")),
        CHERRY(Color.parseColor("#f50057")),
        CORAL(Color.parseColor("#f15b8d")),
        MIDNIGHT(Color.parseColor("#1a237e")),
        LAVENDER(Color.parseColor("#b39ddb")),
        MINT(Color.parseColor("#009c56")),
        GRAPHITE(Color.parseColor("#757575"));

        fun getNameResource(): Int {
            return when (this) {
                SKY -> R.string.tag_color_sky
                GRASS -> R.string.tag_color_grass
                SUNSET -> R.string.tag_color_sunset
                LEMON -> R.string.tag_color_lemon
                SEA -> R.string.tag_color_sea
                GRAPE -> R.string.tag_color_grape
                CHERRY -> R.string.tag_color_cherry
                CORAL -> R.string.tag_color_coral
                MIDNIGHT -> R.string.tag_color_midnight
                MINT -> R.string.tag_color_mint
                LAVENDER -> R.string.tag_color_lavender
                GRAPHITE -> R.string.tag_color_graphite
            }
        }

        companion object {
            private val colors: MutableMap<Int, Tag> = HashMap()

            init {
                for (i in values())
                    colors[i.color] = i
            }

            fun convertColorToTag(int: Int): Tag? = colors[int]

            fun getColors(): IntArray {
                return colors.keys.toIntArray()
            }
        }
    }

    fun tintDrawable(drawable: Drawable?): Drawable? {
        return drawable?.also {
            it.mutate()
            it.colorFilter = BlendModeColorFilterCompat
                .createBlendModeColorFilterCompat(tag.color, BlendModeCompat.SRC_ATOP)
        }
    }

    override fun toJsonString(): String? = JsonDataStreamer.encodeToJson(this, Subject::class.java)

    override fun toJsonFile(destination: File, name: String): File {
        return File(destination, name).apply {
            Okio.buffer(Okio.sink(this)).use {
                toJsonString()?.also { json -> it.write(json.toByteArray()) }
            }
        }
    }

    override fun fromInputStream(inputStream: InputStream) {
        JsonDataStreamer.decodeOnceFromJson(inputStream, Subject::class.java)?.also {
            subjectID = it.subjectID
            code = it.code
            description = it.description
            tag = it.tag
        }
    }

    companion object {

        fun fromInputStream(inputStream: InputStream): Subject {
            return Subject().apply {
                this.fromInputStream(inputStream)
            }
        }
    }

}