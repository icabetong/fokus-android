package com.isaiahvonrundstedt.fokus.features.subject

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.bundleOf
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
import okio.buffer
import okio.sink
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
    var instructor: String? = null,
    @TypeConverters(ColorConverter::class)
    var tag: Tag = Tag.SKY,
    var isSubjectArchived: Boolean = false,
) : Parcelable, Streamable {

    // Used for the color tag of the subject
    @JsonClass(generateAdapter = false)
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
                SKY -> R.string.color_sky
                GRASS -> R.string.color_grass
                SUNSET -> R.string.color_sunset
                LEMON -> R.string.color_lemon
                SEA -> R.string.color_sea
                GRAPE -> R.string.color_grape
                CHERRY -> R.string.color_cherry
                CORAL -> R.string.color_coral
                MIDNIGHT -> R.string.color_midnight
                MINT -> R.string.color_mint
                LAVENDER -> R.string.color_lavender
                GRAPHITE -> R.string.color_graphite
            }
        }

        companion object {
            private val colors: MutableMap<Int, Tag> = HashMap()

            init {
                for (i in values())
                    colors[i.color] = i
            }

            fun convertColorToTag(int: Int): Tag? = colors[int]

            fun getColors(): IntArray = colors.keys.toIntArray()
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
            this.sink().buffer().use {
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
        const val EXTRA_ID = "extra:id"
        const val EXTRA_CODE = "extra:code"
        const val EXTRA_DESCRIPTION = "extra:description"
        const val EXTRA_COLOR = "extra:color"
        const val EXTRA_IS_ARCHIVED = "extra:archived"

        fun toBundle(subject: Subject): Bundle {
            return bundleOf(
                EXTRA_ID to subject.subjectID,
                EXTRA_CODE to subject.code,
                EXTRA_DESCRIPTION to subject.description,
                EXTRA_COLOR to ColorConverter.fromColor(subject.tag),
                EXTRA_IS_ARCHIVED to subject.isSubjectArchived
            )
        }

        fun fromBundle(bundle: Bundle): Subject? {
            if (!bundle.containsKey(EXTRA_ID))
                return null

            return Subject(
                subjectID = bundle.getString(EXTRA_ID)!!,
                code = bundle.getString(EXTRA_CODE),
                description = bundle.getString(EXTRA_DESCRIPTION),
                tag = ColorConverter.toColor(bundle.getInt(EXTRA_COLOR)) ?: Tag.SKY,
                isSubjectArchived = bundle.getBoolean(EXTRA_IS_ARCHIVED)
            )
        }

        fun fromInputStream(inputStream: InputStream): Subject {
            return Subject().apply {
                this.fromInputStream(inputStream)
            }
        }
    }

}