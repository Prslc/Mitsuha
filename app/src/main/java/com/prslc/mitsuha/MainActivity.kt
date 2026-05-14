package com.prslc.mitsuha

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedService.OnScopeEventListener

class MainActivity : Activity(), App.ServiceStateListener {
    private var mService: XposedService? = null
    private lateinit var scopeBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(80, 80, 80, 80)
        }

        scopeBtn = Button(this).apply {
            text = "Initializing service..."
            isEnabled = false
            setOnClickListener { requestMitsuhaScope() }
        }

        root.addView(scopeBtn)
        setContentView(root)
    }

    private fun requestMitsuhaScope() {
        val service = mService ?: return

        // Target packages for batch allocation test
        val targetScopes = listOf(
            "com.miui.securitycenter",
            "com.android.updater",
            "com.miui.home",
            "com.android.providers.downloads"
        )

        service.requestScope(targetScopes, object : OnScopeEventListener {
            override fun onScopeRequestApproved(approved: List<String>) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Batch approved: ${approved.size} packages",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onScopeRequestFailed(message: String) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Batch request failed: $message",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        App.addServiceStateListener(this, true)
    }

    override fun onStop() {
        App.removeServiceStateListener(this)
        super.onStop()
    }

    override fun onServiceStateChanged(service: XposedService?) {
        mService = service
        runOnUiThread {
            if (service != null) {
                scopeBtn.isEnabled = true
                scopeBtn.text = "Batch scope allocation test"
            } else {
                scopeBtn.isEnabled = false
                scopeBtn.text = "Service Disconnected"
            }
        }
    }
}