package com.example.mitsuha

import com.example.mitsuha.handler.MiSafetyHandler
import com.example.mitsuha.handler.PredictiveBackHandler
import com.example.mitsuha.handler.UpdaterHandler
import com.example.mitsuha.resolver.MiSafetyResolver
import com.example.mitsuha.utils.logE
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import org.luckypray.dexkit.DexKitBridge

class MainHook : XposedModule() {

    // init dexkit
    companion object {
        init {
            try {
                System.loadLibrary("dexkit")
            } catch (e: UnsatisfiedLinkError) {
                logE("Failed to load DexKit native library", e)
            }
        }
    }

    override fun onPackageReady(param: PackageReadyParam) {
        super.onPackageReady(param)

        when (param.packageName) {
            "com.android.updater" -> {
                UpdaterHandler(this).onHook(param.classLoader)
            }
            "com.miui.home" -> {
                PredictiveBackHandler(this).onHook(param.classLoader)
            }
            "com.miui.securitycenter" -> {
                val apkPath = param.applicationInfo.sourceDir
                val bridge = DexKitBridge.create(apkPath)
                try {
                    val resolver = MiSafetyResolver()
                    val handler = MiSafetyHandler(this, resolver)
                    handler.findAndHook(bridge, param.classLoader)
                } catch (e: Throwable) {
                    logE("SecurityCenter initialization failed", e)
                } finally {
                    bridge.close()
                }
            }
        }
    }
}
