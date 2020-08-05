package com.isaiahvonrundstedt.fokus.components.utils

import android.content.Context
import android.net.Uri
import org.apache.commons.io.FileUtils
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipArchiveManager private constructor(private var context: Context) {

    private var uri: Uri? = null
    private var items: List<File> = emptyList()

    fun archive() {
        context.contentResolver.openOutputStream(uri!!).use { stream ->
            ZipOutputStream(BufferedOutputStream(stream)).use { zip ->
                items.forEach {
                    BufferedInputStream(FileInputStream(it), BUFFER).use { inputStream ->
                        zip.putNextEntry(ZipEntry(it.name))

                        inputStream.copyTo(zip, BUFFER)
                    }
                }
                zip.flush()
            }
        }
    }

    class Create(context: Context) {
        private var zip = ZipArchiveManager(context)

        fun fromSource(items: List<File>): Create {
            zip.items = items
            return this
        }

        fun toDestination(uri: Uri): Create {
            zip.uri = uri
            return this
        }

        fun compress() = zip.archive()
    }

    companion object {
        private const val BUFFER = 4096
        private const val FILE_TEMP_WORKING_FILE = "temp.fts"

        fun convertInputStream(context: Context, stream: InputStream?): ZipFile {
            val temp = File(context.cacheDir, FILE_TEMP_WORKING_FILE)
            FileUtils.copyToFile(stream, temp)
            return ZipFile(temp)
        }
    }
}