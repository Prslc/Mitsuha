/**
 * Predicts and standardizes back gesture progress.
 * 
 * This handler implements AOSP-style two-phase progress logic, referencing
 * hook points from XiaomiHelper.
 *
 * @see <a href="https://github.com/HowieHChen/XiaomiHelper">XiaomiHelper (GPL v3)</a>
 */

package com.prslc.mitsuha.handler

import android.content.res.Resources.getSystem
import com.prslc.mitsuha.utils.logE
import com.prslc.mitsuha.utils.logI
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.ExceptionMode
import kotlin.math.abs

class PredictiveBackHandler(private val base: XposedInterface) {

    private var linearDistance: Float = 0f
    private var maxDistance: Float = 0f
    private val nonLinearFactor: Float = 0.2f

    fun onHook(loader: ClassLoader) {
        try {
            val density = getSystem().displayMetrics.density
            linearDistance = if (density > 0) density * 412.0f else 1000f
            maxDistance = getSystem().displayMetrics.widthPixels.toFloat()

            val targetClass = loader.loadClass("com.miui.home.recents.GestureStubView")

            // Target the 7-parameter method used for back progress dispatching
            val onBackProgressedMethod = targetClass.getDeclaredMethod(
                "onBackProgressed",
                Float::class.javaPrimitiveType, // x
                Float::class.javaPrimitiveType, // y
                Float::class.javaPrimitiveType, // progress
                Float::class.javaPrimitiveType, // vx
                Float::class.javaPrimitiveType, // vy
                Int::class.javaPrimitiveType,   // edge
                Any::class.java                 // window object
            )

            val fDownX = targetClass.getDeclaredField("mDownX").apply { isAccessible = true }
            val fCurrX = targetClass.getDeclaredField("mCurrX").apply { isAccessible = true }

            base.hook(onBackProgressedMethod).setExceptionMode(ExceptionMode.PROTECTIVE).intercept { chain ->
                val instance = chain.thisObject ?: return@intercept chain.proceed()
                val args = chain.args.toTypedArray()

                try {
                    val mDownX = fDownX.get(instance) as? Float ?: return@intercept chain.proceed()
                    val mCurrX = fCurrX.get(instance) as? Float ?: return@intercept chain.proceed()

                    // AOSP two-phase progress: linear then lerp-based non-linear
                    val deltaX = abs(mCurrX - mDownX)
                    val progress = getProgress(deltaX)

                    // Inject custom progress into the 3rd argument (index 2)
                    args[2] = progress
                } catch (_: Throwable) {
                    // Fail-safe: proceed with original args on error
                }

                chain.proceed(args)
            }

            logI("AOSP two-phase progress hook active.")

        } catch (e: Exception) {
            logE("Setup failed", e)
        }
    }

    // https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-14.0.0_r1/libs/WindowManager/Shell/src/com/android/wm/shell/back/TouchTracker.java
    private fun getProgress(deltaX: Float): Float {
        val linearDist = linearDistance
        val maxDist = maxDistance
        if (maxDist == 0f) return 0f

        return if (linearDist < maxDist) {
            val nonLinearDist = maxDist - linearDist
            val initialTarget = linearDist + nonLinearDist * nonLinearFactor

            if (deltaX <= linearDist) {
                deltaX / initialTarget
            } else {
                val nonLinearDeltaX = deltaX - linearDist
                val nonLinearProgress = nonLinearDeltaX / nonLinearDist
                val currentTarget = initialTarget + (maxDist - initialTarget) * nonLinearProgress
                deltaX / currentTarget
            }
        } else {
            deltaX / maxDist
        }.coerceIn(0f, 1f)
    }
}
