package com.fheicode.mofit

import android.content.Context

object BuildConfigInfo {
    /** Reads the installed versionName, so the screen shows which release is running. */
    fun versionName(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
}
