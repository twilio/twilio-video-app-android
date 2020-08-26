package com.twilio.video.app.util

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat.checkSelfPermission

class PermissionUtil(private val context: Context) {

    fun isPermissionGranted(permission: String) =
            checkSelfPermission(context, permission) == PERMISSION_GRANTED
}
