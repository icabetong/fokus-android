package com.isaiahvonrundstedt.fokus.components.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

class PermissionManager(var context: Context) {

    companion object {
        const val STORAGE_READ_REQUEST_CODE = 3
        const val STORAGE_WRITE_REQUEST_CODE = 4

        fun requestReadStoragePermission(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_READ_REQUEST_CODE)
        }

        fun requestWriteStoragePermission(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_WRITE_REQUEST_CODE)
        }
    }

    val readStorageGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            else true
        }

    val writeStorageGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            else true
        }
}