package com.prslc.mitsuha

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArraySet

class App : Application() {
    private val TAG = "Mitsuha-Test"

    companion object {
        private var mService: XposedService? = null
        private val listeners = CopyOnWriteArraySet<ServiceStateListener>()

        fun addServiceStateListener(listener: ServiceStateListener, notifyNow: Boolean) {
            listeners.add(listener)
            if (notifyNow) listener.onServiceStateChanged(mService)
        }

        fun removeServiceStateListener(listener: ServiceStateListener) {
            listeners.remove(listener)
        }
    }

    interface ServiceStateListener {
        fun onServiceStateChanged(service: XposedService?)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: $name")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "Service Disconnected")
            mService = null
            notifyListeners(null)
        }
    }

    override fun onCreate() {
        super.onCreate()

        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                Log.i(TAG, "Service Bound Successfully")
                mService = service
                notifyListeners(service)
            }

            override fun onServiceDied(service: XposedService) {
                mService = null
                notifyListeners(null)
            }
        })

        bindXposedService()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun bindXposedService() {
        val intent = Intent("io.github.libxposed.service.ACTION_BIND_SERVICE")
        val resolvedInfo = packageManager.queryIntentServices(intent, 0)

        if (resolvedInfo.isNotEmpty()) {
            val serviceInfo = resolvedInfo[0].serviceInfo
            intent.setComponent(ComponentName(serviceInfo.packageName, serviceInfo.name))

            try {
                bindService(intent, connection, BIND_AUTO_CREATE)
            } catch (e: Exception) {
                Log.e(TAG, "Bind failed", e)
            }
        } else {
            Log.e(TAG, "Xposed Service not found")
        }
    }

    private fun notifyListeners(service: XposedService?) {
        listeners.forEach { it.onServiceStateChanged(service) }
    }
}