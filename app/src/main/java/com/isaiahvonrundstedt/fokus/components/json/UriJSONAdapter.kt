package com.isaiahvonrundstedt.fokus.components.json

import android.net.Uri
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class UriJSONAdapter {

    @FromJson
    fun toUri(string: String): Uri {
        return Uri.parse(string)
    }

    @ToJson
    fun fromUri(uri: Uri): String {
        return uri.toString()
    }

}