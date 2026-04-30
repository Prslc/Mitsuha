package com.example.mitsuha.resolver

import org.luckypray.dexkit.DexKitBridge

class MiSafetyResolver {

    private var cachedMethodInfo: MethodInfo? = null

    data class MethodInfo(
        val name: String,
        val descriptor: String
    )

    fun resolve(bridge: DexKitBridge): MethodInfo? {
        if (cachedMethodInfo != null) return cachedMethodInfo

        return try {
            val method = bridge.findMethod {
                matcher {
                    declaredClass("com.xiaomi.security.xsof.MiSafetyDetectService")
                    returnType = "void"
                    paramTypes("java.lang.Object")
                    usingStrings("internal error, task type : ")
                }
            }.single()

            cachedMethodInfo = MethodInfo(method.name, method.descriptor)
            cachedMethodInfo
        } catch (_: Exception) {
            null
        }
    }
}
