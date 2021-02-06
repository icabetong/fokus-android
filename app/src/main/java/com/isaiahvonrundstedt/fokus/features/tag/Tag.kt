package com.isaiahvonrundstedt.fokus.features.tag

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "tags")
data class Tag @JvmOverloads constructor(
    @PrimaryKey
    var tagID: String = UUID.randomUUID().toString(),
    var name: String? = null
): Parcelable {


}
