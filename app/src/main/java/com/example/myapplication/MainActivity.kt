package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.MyApplicationTheme

import android.os.Build
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- 1. 本地安全檢查 (Anti-Debug & Anti-Emulator) ---
        if (isDebuggerConnected() || isEmulator()) {
            exitProcess(0)
        }

        // --- 2. Google Play Integrity 「加固」檢查 ---
        checkGooglePlayIntegrity()

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FractionScreen()
                }
            }
        }
    }

    // --- 安全檢查實作 ---
    private fun isDebuggerConnected(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }

    private fun checkGooglePlayIntegrity() {
        try {
            val integrityManager = IntegrityManagerFactory.create(applicationContext)
            val nonce = "fraction_tool_" + System.currentTimeMillis()
            val integrityTokenRequest = IntegrityTokenRequest.builder()
                .setNonce(nonce)
                .build()

            integrityManager.requestIntegrityToken(integrityTokenRequest)
                .addOnSuccessListener { _ ->
                    Log.d("Security", "Google Play Integrity 檢查請求成功。")
                }
                .addOnFailureListener { e ->
                    Log.e("Security", "Google Play Integrity 檢查失敗: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("Security", "Integrity SDK 初始化失敗")
        }
    }
}
