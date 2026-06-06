package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class FractionViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // --- 管理員：雲端配置 ---
    private val FINAL_CLOUD_URL = "https://pastebin.com/raw/BqMQbvcK"
    private val PASTEBIN_API_KEY = "6NHVR4kS6Hlj2RUY_M6P06YaoDl2qvQo"

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _steps = MutableStateFlow("")
    val steps: StateFlow<String> = _steps.asStateFlow()

    private val _shouldSimplify = MutableStateFlow(true)
    val shouldSimplify: StateFlow<Boolean> = _shouldSimplify.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    data class UpdateInfo(val ver: String, val log: String, val isForce: Boolean, val url: String, var isEnabled: Boolean = true)
    private val _updateNotice = MutableStateFlow<UpdateInfo?>(null)
    val updateNotice: StateFlow<UpdateInfo?> = _updateNotice.asStateFlow()

    private val _cloudStatus = MutableStateFlow("尚未連結雲端")
    val cloudStatus: StateFlow<String> = _cloudStatus.asStateFlow()

    init { checkUpdates() }

    fun checkUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = URL(FINAL_CLOUD_URL).readText()
                val json = JSONObject(content)
                withContext(Dispatchers.Main) {
                    _updateNotice.value = UpdateInfo(
                        ver = json.getString("version"),
                        log = json.getString("log"),
                        isForce = json.getBoolean("force"),
                        url = json.getString("url")
                    )
                    _cloudStatus.value = "同步成功！目前雲端版本：${json.getString("version")}"
                }
            } catch (e: Exception) {
                _cloudStatus.value = "雲端讀取失敗 (檢查網路)"
            }
        }
    }

    // --- 【一鍵發布到 Pastebin】 ---
    fun publishToCloud(ver: String, log: String, isForce: Boolean, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _cloudStatus.value = "正在打包數據並發布..."
                val json = JSONObject().apply {
                    put("version", ver)
                    put("log", log)
                    put("force", isForce)
                    put("url", url)
                }.toString()

                val postData = "api_dev_key=$PASTEBIN_API_KEY" +
                        "&api_option=paste" +
                        "&api_paste_code=" + URLEncoder.encode(json, "UTF-8") +
                        "&api_paste_name=" + URLEncoder.encode("App_Config", "UTF-8") +
                        "&api_paste_format=json" +
                        "&api_paste_private=1" // 設為 Unlisted

                val conn = URL("https://pastebin.com/api/api_post.php").openConnection()
                conn.doOutput = true
                conn.getOutputStream().write(postData.toByteArray())
                val response = conn.getInputStream().bufferedReader().readText()

                withContext(Dispatchers.Main) {
                    if (response.startsWith("http")) {
                        _cloudStatus.value = "發布成功！請將此網址設為源：\n$response"
                    } else {
                        _cloudStatus.value = "發布失敗：$response"
                    }
                }
            } catch (e: Exception) {
                _cloudStatus.value = "連線異常：${e.message}"
            }
        }
    }

    fun tryLogin(pass: String): Boolean = if (pass == "398666") { _isAdminMode.value = true; true } else false
    fun exitAdmin() { _isAdminMode.value = false }
    fun dismissUpdate() {
        val current = _updateNotice.value
        if (current != null && !current.isForce) { _updateNotice.value = current.copy(isEnabled = false) }
    }

    fun onOcrResult(eq: OcrUtils.FractionEquation) {
        calculate(eq.n1.toString(), eq.d1.toString(), eq.n2.toString(), eq.d2.toString(), eq.operator)
    }

    fun setShouldSimplify(v: Boolean) { _shouldSimplify.value = v }

    fun quickSimplify(n: String, d: String) {
        val f = createFraction(n, d) ?: return
        val res = f.simplify()
        _result.value = "最簡：$res"; _steps.value = "過程：求得 GCD 後化簡為 $res"
    }

    fun calculate(n1: String, d1: String, n2: String, d2: String, operation: String) {
        val f1 = createFraction(n1, d1); val f2 = createFraction(n2, d2)
        if (f1 == null || f2 == null) { _result.value = "輸入錯誤"; return }
        val s = _shouldSimplify.value
        val op = when(operation) { "*" -> "×"; "/" -> "÷"; "x" -> "×"; "＋" -> "+"; "－" -> "-"; else -> operation }
        
        when (op) {
            "+" -> { val r = f1.addRaw(f2); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = getAdditionSteps(f1,f2,s) }
            "-" -> { val r = f1.minusRaw(f2); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = getSubtractionSteps(f1,f2,s) }
            "×" -> { val r = Fraction(f1.numerator*f2.numerator, f1.denominator*f2.denominator); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = "計算：($f1) × ($f2) = $r" }
            "÷" -> { if(f2.numerator==0L){_result.value="除數不為0";return}; val r = Fraction(f1.numerator*f2.denominator, f1.denominator*f2.numerator); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = "計算：($f1) ÷ ($f2) = $r" }
            "比較" -> { 
                val v1 = f1.numerator.toDouble() / f1.denominator; val v2 = f2.numerator.toDouble() / f2.denominator
                _result.value = if (v1 > v2) "$f1 > $f2" else if (v1 < v2) "$f1 < $f2" else "$f1 = $f2"
                _steps.value = "比較：$f1 (${String.format("%.2f", v1)}) 與 $f2 (${String.format("%.2f", v2)})"
            }
        }
    }

    private fun createFraction(n: String, d: String): Fraction? = try {
        val num = n.toLong(); val den = d.toLong()
        if (den == 0L) null else Fraction(num, den)
    } catch (e: Exception) { null }
}
