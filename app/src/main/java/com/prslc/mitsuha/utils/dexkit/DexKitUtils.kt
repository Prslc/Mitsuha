package com.prslc.mitsuha.utils.dexkit

import android.annotation.SuppressLint
import android.content.Context
import com.prslc.mitsuha.utils.logE
import io.github.libxposed.api.XposedModule
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.annotations.DexKitExperimentalApi
import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Method

/**
 * Utility object providing a streamlined pipeline for resolving dex signatures into
 * standard Java reflection elements with cross-process persistent caching capability.
 */
object DexKitUtils {

    @Volatile
    private var isBridgeInitialized = false

    /**
     * Helper to reflectively fetch the current process context within the host app.
     */
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun getCurrentContext(): Context {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
        val currentActivityThread = currentActivityThreadMethod.invoke(null)
        val getSystemContextMethod = activityThreadClass.getDeclaredMethod("getSystemContext")
        return getSystemContextMethod.invoke(currentActivityThread) as Context
    }

    /**
     * Resolves a target method signature into a standard Java [Method] instance.
     * This function manages the lifecycle of [DexKitCacheBridge.RecyclableBridge] automatically,
     * injects a multi-process persistent cache implementation if not initialized, isolates cache
     * entries based on the host application's version code, and safely disposes of native C++ resources.
     *
     * @param module The active [XposedModule] instance for logging operations.
     * @param apkPath The absolute filesystem path to the target APK binary.
     * @param tagPrefix A unique string identifier representing the logical business unit (e.g., "gboard").
     * @param block A functional lambda providing a scoped, ready-to-use bridge environment.
     * @return The successfully resolved standard Java [Method], or `null` if the pipeline fails.
     */
    @OptIn(DexKitExperimentalApi::class)
    fun resolveMethod(
        module: XposedModule,
        apkPath: String,
        tagPrefix: String,
        block: (DexKitCacheBridge.RecyclableBridge) -> DexMethod?
    ): Method? {
        val context = try {
            getCurrentContext()
        } catch (e: Exception) {
            logE("Failed to acquire host context from ActivityThread", e)
            return null
        }

        val classLoader = context.classLoader

        val versionCode = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            0L
        }

        if (!isBridgeInitialized) {
            synchronized(DexKitCacheBridge::class.java) {
                if (!isBridgeInitialized) {
                    try {
                        DexKitCacheBridge.idleTimeoutMillis = 3000L
                        DexKitCacheBridge.cachePolicy = DexKitCacheBridge.CachePolicy(
                            cacheSuccess = true,
                            failurePolicy = DexKitCacheBridge.CacheFailurePolicy.QUERY_ONLY
                        )
                        DexKitCacheBridge.init(DexKitCache(context))
                        isBridgeInitialized = true
                    } catch (e: IllegalStateException) {
                        isBridgeInitialized = true
                    }
                }
            }
        }

        val appTag = "$tagPrefix:$versionCode"

        return try {
            DexKitCacheBridge.create(appTag, apkPath).use { bridge ->
                val dexMethod = block(bridge)
                dexMethod?.toReflectMethod(classLoader)
            }
        } catch (e: Throwable) {
            logE("Failed to resolve method for tag: $appTag", e)
            null
        }
    }
}