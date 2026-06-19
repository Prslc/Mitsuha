package com.prslc.mitsuha.resolver

import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.annotations.DexKitExperimentalApi
import org.luckypray.dexkit.wrap.DexMethod

class MiSafetyResolver {

    @OptIn(DexKitExperimentalApi::class)
    fun resolve(bridge: DexKitCacheBridge.RecyclableBridge): DexMethod? {
        return bridge.getMethodOrNull("misafety_detect_method") {
            matcher {
                declaredClass("com.xiaomi.security.xsof.MiSafetyDetectService")
                returnType = "void"
                paramTypes = listOf("java.lang.Object")
                usingStrings("internal error, task type : ")
            }
        }
    }
}