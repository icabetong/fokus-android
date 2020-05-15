package com.isaiahvonrundstedt.fokus.features.shared

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class PermissionManager(var context: Context) {

    companion object {
        const val readStorageRequestCode = 3
        const val writeStorageRequestCode = 2
    }

    val readAccessGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            else
                true
        }

    val writeAccessGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            else true
        }
}