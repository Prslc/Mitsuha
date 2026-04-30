package com.example.mitsuha.handler

import com.example.mitsuha.resolver.MiSafetyResolver
import com.example.mitsuha.utils.logE
import com.example.mitsuha.utils.logI
import io.github.libxposed.api.XposedInterface
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Field

class MiSafetyHandler(
    private val base: XposedInterface,
    private val resolver: MiSafetyResolver
) {

    private var cachedIntField: Field? = null

    fun findAndHook(bridge: DexKitBridge, loader: ClassLoader) {
        val info = resolver.resolve(bridge)
            ?: return logE("Target method not found in MiSafetyDetectService")
        onHook(loader, info.name)
    }

    fun onHook(loader: ClassLoader, methodName: String) {
        val targetClass = loader.loadClass("com.xiaomi.security.xsof.MiSafetyDetectService")
        val methodL = targetClass.getDeclaredMethod(methodName, Any::class.java)

        base.hook(methodL)
            .setPriority(XposedInterface.PRIORITY_DEFAULT)
            .intercept { chain ->
                val taskObj = chain.args[0]
                if (taskObj != null) {
                    interceptTask(taskObj)
                }
                chain.proceed()
            }

        logI("Hook mounted successfully: $methodName")
    }

    private fun interceptTask(taskObj: Any) {
        try {
            if (cachedIntField == null) {
                cachedIntField = taskObj.javaClass.declaredFields
                    .firstOrNull { it.type == Int::class.java }
            }

            cachedIntField?.let { field ->
                field.isAccessible = true
                field.setInt(taskObj, 11)
            }
        } catch (e: Throwable) {
            logE("Field modification failed", e)
        }
    }
}
