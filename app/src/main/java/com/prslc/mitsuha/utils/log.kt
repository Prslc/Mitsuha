package com.prslc.mitsuha.utils

import android.util.Log
import com.prslc.mitsuha.BuildConfig
import io.github.libxposed.api.XposedInterface

var logPrefix: String = ""
const val LOG_TAG = "Mitsuha"

var xposedLog: XposedInterface? = null

private fun xposedLog(priority: Int, msg: String, t: Throwable? = null) {
    xposedLog?.log(priority, LOG_TAG, "$logPrefix$msg", t)
}

fun logD(msg: String, t: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.d(LOG_TAG, "$logPrefix$msg", t)
        xposedLog(Log.DEBUG, msg, t)
    }
}

fun logE(msg: String, t: Throwable? = null) {
    Log.e(LOG_TAG, "$logPrefix$msg", t)
    xposedLog(Log.ERROR, msg, t)
}

fun logI(msg: String, t: Throwable? = null) {
    Log.i(LOG_TAG, "$logPrefix$msg", t)
    xposedLog(Log.INFO, msg, t)
}

fun logW(msg: String, t: Throwable? = null) {
    Log.w(LOG_TAG, "$logPrefix$msg", t)
    xposedLog(Log.WARN, msg, t)
}
