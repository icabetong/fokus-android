package com.isaiahvonrundstedt.fokus.features.core.extensions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.getFileName(context: Context): String {
    var result = ""
    if (this.scheme == "content") {
        val cursor: Cursor? = context.contentResolver?.query(this, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst())
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } catch (ex: Exception) {}
        finally { cursor?.close() }
    } else {
        result = this.path.toString()
        val index = result.lastIndexOf('/')
        if (index != 1)
            result = result.substring(index + 1)
    }
    return result
}