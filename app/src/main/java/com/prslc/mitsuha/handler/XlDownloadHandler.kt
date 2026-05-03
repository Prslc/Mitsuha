/**
 * Prevents the Xunlei download engine from creating the .xlDownload directory
 * on shared storage.
 *
 * The original idea (hooking setDebug + FileUtil.createFile) is from WOMMO.
 * This implementation uses a different approach — hooking getDpDebugLogPath
 * and saveDpDebugLogPath — to avoid touching SharedPreferences.
 *
 * @see <a href="https://github.com/YifePlayte/WOMMO">WOMMO (GPL v3)</a>
 */

package com.prslc.mitsuha.handler

import android.annotation.SuppressLint
import com.prslc.mitsuha.utils.logD
import com.prslc.mitsuha.utils.logE
import com.prslc.mitsuha.utils.logI
import io.github.libxposed.api.XposedInterface

class XlDownloadHandler(private val base: XposedInterface) {

    @SuppressLint("PrivateApi")
    fun onHook(loader: ClassLoader) {
        try {
            val settingsClass = loader.loadClass(
                $$"com.android.providers.downloads.config.DownloadSettings$XLSecureConfigSettings"
            )

            val getPath = settingsClass.getDeclaredMethod("getDpDebugLogPath", String::class.java)
            val savePath = settingsClass.getDeclaredMethod("saveDpDebugLogPath", String::class.java)

            // getDpDebugLogPath reads the persisted log path from SharedPreferences.
            // Even with debug off, setDebug() still uses this path to create files
            // under /sdcard/.xlDownload/. Returning "" forces setDebug() to hit its
            // own fallback: context.getCacheDir() + "/dp.log", which stays private.
            base.hook(getPath).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val result = chain.proceed() as? String ?: return chain.proceed()
                    if (result.contains(".xlDownload")) {
                        logD("getDpDebugLogPath: blocked .xlDownload path, forcing fallback to cache")
                        return ""
                    }
                    return result
                }
            })

            // setDebug() writes the resolved path back to SharedPreferences after use.
            // We drop that write so the SP entry stays as-is — we don't touch user data.
            base.hook(savePath).intercept {
                logD("saveDpDebugLogPath: suppressed SP write")
                false
            }

            logI("xlDownload hooks installed")

        } catch (e: Exception) {
            logE("xlDownload: hook failed", e)
        }
    }
}
