package com.isaiahvonrundstedt.fokus.features.shared

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter

class PermissionManager(var context: Context) {

    companion object {
        const val storageRequestCode = 3
    }

    val storageReadGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            else true
        }
}