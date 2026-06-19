package com.prslc.mitsuha.handler

import com.prslc.mitsuha.utils.logE
import com.prslc.mitsuha.utils.logI
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Field
import java.lang.reflect.Method

class MiSafetyHandler(private val module: XposedModule) {
    private var cachedIntField: Field? = null

    fun onHook(method: Method) {
        module.hook(method)
            .setPriority(XposedInterface.PRIORITY_DEFAULT)
            .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
            .intercept { chain ->
                val taskObj = chain.args[0]
                if (taskObj != null) {
                    interceptTask(taskObj)
                }
                chain.proceed()
            }

        logI("Hook mounted successfully: ${method.name}")
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