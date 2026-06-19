package com.prslc.mitsuha.utils.dexkit

import android.content.Context
import android.content.SharedPreferences
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.annotations.DexKitExperimentalApi

/**
 * SharedPreferences-backed implementation of [DexKitCacheBridge.Cache].
 * Utilizes `MODE_MULTI_PROCESS` to allow cross-process cache synchronization.
 *
 * @property "context" The application context used to retrieve the shared preferences.
 */
@OptIn(DexKitExperimentalApi::class)
class DexKitCache(context: Context) : DexKitCacheBridge.Cache {

    @Suppress("DEPRECATION")
    private val sp: SharedPreferences = context.getSharedPreferences(
        "dexkit_cache",
        Context.MODE_PRIVATE or Context.MODE_MULTI_PROCESS
    )

    override fun getString(key: String, default: String?): String? =
        sp.getString(key, default)

    override fun putString(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    override fun getStringList(key: String, default: List<String>?): List<String>? {
        val rawData = sp.getString(key, null) ?: return default
        if (rawData.isEmpty()) return emptyList()
        return rawData.split("\u001F")
    }

    override fun putStringList(key: String, value: List<String>) {
        val encoded = value.joinToString("\u001F")
        sp.edit().putString(key, encoded).apply()
    }

    override fun remove(key: String) {
        sp.edit().remove(key).apply()
    }

    override fun getAllKeys(): Collection<String> =
        sp.all.keys

    override fun clearAll() {
        sp.edit().clear().apply()
    }
}