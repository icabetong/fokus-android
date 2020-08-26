package com.isaiahvonrundstedt.fokus.components.utils

import android.content.Context
import android.net.Uri
import org.apache.commons.io.FileUtils
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class DataArchiver private constructor(private var context: Context) {

    private var uri: Uri? = null
    private var destination: File? = null
    private var items = mutableListOf<File>()

    fun archive() {
        if (destination != null) {
            FileOutputStream(destination).use {
                ZipOutputStream(BufferedOutputStream(it)).use { outputStream ->
                    setEntries(outputStream)
                }
            }
        } else if (uri != null) {
            context.contentResolver.openOutputStream(uri!!)?.use {
                ZipOutputStream(BufferedOutputStream(it)).use { outputStream ->
                    setEntries(outputStream)
                }
            }
        }
    }

    private fun setEntries(zip: ZipOutputStream) {
        items.forEach {
            if (it.isDirectory) {
                zip.putNextEntry(ZipEntry("${it.name}/"))
                for (source: File in it.listFiles()!!) {
                    onCopyToOutputStream(zip, "${it.name}/${source.name}", source)
                }
            } else onCopyToOutputStream(zip, it.name, it)
        }
    }

    private fun onCopyToOutputStream(zip: ZipOutputStream, entryName: String, source: File) {
        BufferedInputStream(FileInputStream(source), BUFFER).use { stream ->
            zip.putNextEntry(ZipEntry(entryName))
            stream.copyTo(zip, BUFFER)
        }
    }


    class Create(context: Context) {
        private var zip = DataArchiver(context)

        fun addSource(items: List<File>): Create {
            zip.items.addAll(items)
            return this
        }

        fun addSource(file: File): Create {
            zip.items.add(file)
            return this
        }

        fun toDestination(uri: Uri): Create {
            zip.uri = uri
            return this
        }

        fun toDestination(file: File): Create {
            zip.destination = file
            return this
        }

        fun start() = zip.archive()
    }

    companion object {
        private const val BUFFER = 4096
        private const val FILE_TEMP_WORKING_FILE = "temp.fts"

        fun parseInputStream(context: Context, stream: InputStream?): ZipFile {
            val temp = File(context.cacheDir, FILE_TEMP_WORKING_FILE)
            FileUtils.copyToFile(stream, temp)
            return ZipFile(temp)
        }
    }
}