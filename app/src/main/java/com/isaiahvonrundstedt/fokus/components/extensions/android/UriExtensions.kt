package com.isaiahvonrundstedt.fokus.components.extensions.android

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

/**
 *  Extension function used to get the file name
 *  of an uri object
 *  @param context used to get access for a contentResolver object
 */
fun Uri.getFileName(context: Context): String {
    var result = ""
    if (this.scheme == "content") {
        val cursor: Cursor? = context.contentResolver?.query(this, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst())
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } catch (ex: Exception) {
        } finally {
            cursor?.close()
        }
    } else {
        result = this.path.toString()
        val index = result.lastIndexOf('/')
        if (index != 1)
            result = result.substring(index + 1)
    }
    return result
}