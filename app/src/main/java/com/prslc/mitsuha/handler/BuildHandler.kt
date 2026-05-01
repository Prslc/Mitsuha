/**
 * Bypasses system build consistency checks to suppress "System Corrupted" alerts.
 *
 * @see <a href="https://github.com/AlexLiuDev233/StopVintf">StopVintf (GPL v2)</a>
 */

package com.prslc.mitsuha.handler

import android.annotation.SuppressLint
import com.prslc.mitsuha.utils.logD
import com.prslc.mitsuha.utils.logE
import com.prslc.mitsuha.utils.logI
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable

class BuildHandler(private val base: XposedInterface) {

    @SuppressLint("SoonBlockedPrivateApi")
    fun onHook(loader: ClassLoader) {
        try {
            val targetClass = loader.loadClass("android.os.Build")

            val method = targetClass.getDeclaredMethod("isBuildConsistent")

            base.hook(method as Executable).intercept { _ ->
                logD("Intercepted isBuildConsistent, returning true")
                true
            }

            logI("BuildHandler initialized: System consistency check bypassed")

        } catch (e: NoSuchMethodException) {
            // May fail on API 36+ due to Hidden API restrictions. Future-proof with DexKit if needed.
            logE("BuildHandler: Method isBuildConsistent not found. Target API might have changed.", e)
        } catch (e: Exception) {
            logE("BuildHandler: Unexpected error during hook", e)
        }
    }
}
