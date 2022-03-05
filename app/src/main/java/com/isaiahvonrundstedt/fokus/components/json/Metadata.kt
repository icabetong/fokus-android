package com.isaiahvonrundstedt.fokus.components.json

import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.squareup.moshi.JsonClass
import okio.buffer
import okio.sink
import java.io.File
import java.io.InputStream

/**
 *   Metadata class handles exported json
 *   data the is packaged through zip.
 *   It reads what data are inside the package,
 *   and if the corresponding data is compatible
 *   with the application.
 */
@JsonClass(generateAdapter = true)
data class Metadata @JvmOverloads constructor(
    var appVersion: Int = BuildConfig.VERSION_CODE,
    var appBuildName: String? = BuildConfig.VERSION_NAME,
    var databaseVersion: Int = AppDatabase.DATABASE_VERSION,
    var data: String? = null
) : Streamable {

    fun verify(dataString: String): Boolean {
        return databaseVersion == AppDatabase.DATABASE_VERSION &&
                data == dataString
    }

    override fun toJsonString(): String? {
        return JsonDataStreamer.encodeToJson(this, Metadata::class.java)
    }

    override fun toJsonFile(destination: File, name: String): File {
        return File(destination, name).apply {
            this.sink().buffer().use {
                toJsonString()?.also { json -> it.write(json.toByteArray()) }
            }
        }
    }

    override fun fromInputStream(inputStream: InputStream) {
        JsonDataStreamer.decodeOnceFromJson(inputStream, Metadata::class.java)?.also {
            appVersion = it.appVersion
            appBuildName = it.appBuildName
            databaseVersion = it.databaseVersion
            data = it.data
        }
    }

    companion object {
        const val DATA_BUNDLE = "data:bundle"
        const val DATA_TASK = "data:task"
        const val DATA_SUBJECT = "data:subject"
        const val DATA_EVENT = "data:event"
        const val DATA_LOG = "data:log"

        const val FILE_NAME = "metadata.json"

        fun fromInputStream(inputStream: InputStream): Metadata {
            return Metadata().apply {
                this.fromInputStream(inputStream)
            }
        }
    }

}