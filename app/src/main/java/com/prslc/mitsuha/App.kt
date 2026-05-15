package com.prslc.mitsuha

import android.app.Application
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

    override fun onCreate() {
        super.onCreate()

        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                Log.i(TAG, "Service bind Successfully")
                mService = service
            }

            override fun onServiceDied(service: XposedService) {
                mService = null
            }
        })
    }
}