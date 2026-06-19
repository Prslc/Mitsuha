package com.prslc.mitsuha.utils.dexkit

import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Method

/**
 * Resolves a [DexMethod] wrapper into a standard Java [Method] instance
 * using the provided [ClassLoader].
 *
 * @param classLoader The class loader to resolve the target class and method.
 * @return The resolved Java [Method], or `null` if resolution fails, or it represents a constructor.
 */
fun DexMethod.toReflectMethod(classLoader: ClassLoader): Method? {
    return try {
        if (this.isConstructor) {
            null
        } else {
            this.getMethodInstance(classLoader)
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Maps a collection of [DexMethod] wrappers to a list of successfully
 * resolved Java [Method] instances.
 *
 * @param classLoader The class loader to resolve the target classes and methods.
 * @return A filtered list of successfully resolved Java [Method] objects.
 */
fun Collection<DexMethod>.toReflectMethods(classLoader: ClassLoader): List<Method> {
    return this.mapNotNull { method -> method.toReflectMethod(classLoader) }
}