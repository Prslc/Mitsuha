package com.prslc.mitsuha.handler

import android.annotation.SuppressLint
import com.prslc.mitsuha.utils.logD
import com.prslc.mitsuha.utils.logE
import com.prslc.mitsuha.utils.logI
import io.github.libxposed.api.XposedInterface
import org.json.JSONObject
import java.lang.reflect.Executable

class UpdaterHandler(private val base: XposedInterface) {

    @SuppressLint("PrivateApi")
    fun onHook(loader: ClassLoader) {
        try {
            val targetClass = loader.loadClass("com.android.updater.UpdateInfo")

            // Hook getRomInfoFromJson
            val methodFromJson = targetClass.getDeclaredMethod(
                "getRomInfoFromJson",
                JSONObject::class.java,
                String::class.java,
                HashSet::class.java
            )

            base.hook(methodFromJson as Executable).intercept { _ ->
                logD("Intercepted getRomInfoFromJson, returning null")
                null
            }

            // Hook getRomInfoByType
            val methodByType = targetClass.getDeclaredMethod(
                "getRomInfoByType",
                Int::class.javaPrimitiveType
            )

            base.hook(methodByType as Executable).intercept { _ ->
                logD("Intercepted getRomInfoByType, returning null")
                null
            }

            logI("Update intercepted successfully")

        } catch (e: Exception) {
            logE("Hook failed in UpdaterHandler", e)
        }
    }
}
