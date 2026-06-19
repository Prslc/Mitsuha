package com.prslc.mitsuha

import com.prslc.mitsuha.handler.BuildHandler
import com.prslc.mitsuha.handler.MiSafetyHandler
import com.prslc.mitsuha.handler.PredictiveBackHandler
import com.prslc.mitsuha.handler.UpdaterHandler
import com.prslc.mitsuha.handler.XlDownloadHandler
import com.prslc.mitsuha.resolver.MiSafetyResolver
import com.prslc.mitsuha.utils.dexkit.DexKitUtils
import com.prslc.mitsuha.utils.logE
import com.prslc.mitsuha.utils.xposedLog
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import org.luckypray.dexkit.annotations.DexKitExperimentalApi

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

    // Set global XposedInterface reference early, once per process.
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        super.onModuleLoaded(param)
        xposedLog = this
    }

    // System framework hooks need use onSystemServerStarting instead of onPackageReady.
    // This callback fires once when system_server boots and provides the correct classloader.
    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        super.onSystemServerStarting(param)
        BuildHandler(this).onHook(param.classLoader)
    }

    @OptIn(DexKitExperimentalApi::class)
    override fun onPackageReady(param: PackageReadyParam) {
        super.onPackageReady(param)

        when (param.packageName) {
            "com.android.updater" -> {
                UpdaterHandler(this).onHook(param.classLoader)
            }
            "com.miui.home" -> {
                PredictiveBackHandler(this).onHook(param.classLoader)
            }
            "com.android.providers.downloads" -> {
                XlDownloadHandler(this).onHook(param.classLoader)
            }
            "com.miui.securitycenter" -> {
                val apkPath = param.applicationInfo.sourceDir

                val reflectMethod = DexKitUtils.resolveMethod(this, apkPath, "securitycenter") { bridge ->
                    MiSafetyResolver().resolve(bridge)
                }

                if (reflectMethod != null) {
                    try {
                        MiSafetyHandler(this).onHook(reflectMethod)
                    } catch (e: Throwable) {
                        logE("SecurityCenter initialization failed", e)
                    }
                }
            }
        }
    }
}
