package com.example.mitsuha.handler

import com.example.mitsuha.utils.logD
import com.example.mitsuha.utils.logE
import com.example.mitsuha.utils.logI
import io.github.libxposed.api.XposedInterface
import org.json.JSONObject
import java.lang.reflect.Executable

class UpdaterHandler(private val base: XposedInterface) {

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
