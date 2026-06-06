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

class FractionViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // --- 【管理員：已寫死雲端網址，清除數據也有效】 ---
    private val FINAL_CLOUD_URL = "https://pastebin.com/raw/BqMQbvcK"

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _steps = MutableStateFlow("")
    val steps: StateFlow<String> = _steps.asStateFlow()

    private val _shouldSimplify = MutableStateFlow(true)
    val shouldSimplify: StateFlow<Boolean> = _shouldSimplify.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    data class UpdateInfo(
        val ver: String, val log: String, val isForce: Boolean, val url: String, var isEnabled: Boolean = true
    )
    private val _updateNotice = MutableStateFlow<UpdateInfo?>(null)
    val updateNotice: StateFlow<UpdateInfo?> = _updateNotice.asStateFlow()

    private val _cloudStatus = MutableStateFlow("正在從雲端讀取指令...")
    val cloudStatus: StateFlow<String> = _cloudStatus.asStateFlow()

    init {
        checkUpdates()
    }

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
                    _cloudStatus.value = "雲端同步成功！最新版本：${json.getString("version")}"
                }
            } catch (e: Exception) {
                _cloudStatus.value = "連線失敗: ${e.message}"
            }
        }
    }

    fun tryLogin(password: String): Boolean {
        return if (password == "398666") { _isAdminMode.value = true; true } else false
    }

    fun exitAdmin() { _isAdminMode.value = false }
    
    fun dismissUpdate() {
        val current = _updateNotice.value
        if (current != null && !current.isForce) {
            _updateNotice.value = current.copy(isEnabled = false)
        }
    }

    fun onOcrResult(eq: OcrUtils.FractionEquation) {
        val op = when(eq.operator) { "*" -> "×"; "/" -> "÷"; "x" -> "×"; else -> eq.operator }
        calculate(eq.n1.toString(), eq.d1.toString(), eq.n2.toString(), eq.d2.toString(), op)
    }

    fun setShouldSimplify(v: Boolean) { _shouldSimplify.value = v }

    fun quickSimplify(n: String, d: String) {
        val f = createFraction(n, d)
        if (f == null) { _result.value = "請輸入正確的分數"; return }
        val res = f.simplify()
        _result.value = "化簡結果：$res"
        _steps.value = "過程：求分子分母最大公因數並約分，得 $res"
    }

    fun calculate(n1: String, d1: String, n2: String, d2: String, operation: String) {
        val f1 = createFraction(n1, d1); val f2 = createFraction(n2, d2)
        if (f1 == null || f2 == null) { _result.value = "格式錯誤"; return }
        val s = _shouldSimplify.value
        val normalizedOp = when(operation) { "*" -> "×"; "/" -> "÷"; "x" -> "×"; "＋" -> "+"; "－" -> "-"; else -> operation }
        
        when (normalizedOp) {
            "+" -> { val r = f1.addRaw(f2); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = getAdditionSteps(f1,f2,s) }
            "-" -> { val r = f1.minusRaw(f2); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = getSubtractionSteps(f1,f2,s) }
            "×" -> { val r = Fraction(f1.numerator*f2.numerator, f1.denominator*f2.denominator); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = "計算：($f1) × ($f2) = $r" }
            "÷" -> { if(f2.numerator==0L){_result.value="不可除0";return}; val r = Fraction(f1.numerator*f2.denominator, f1.denominator*f2.numerator); _result.value = "結果：${if(s)r.simplify() else r}"; _steps.value = "計算：($f1) ÷ ($f2) = $r" }
            "比較" -> { 
                val val1 = f1.numerator.toDouble() / f1.denominator
                val val2 = f2.numerator.toDouble() / f2.denominator
                _result.value = if (val1 > val2) "$f1 > $f2" else if (val1 < val2) "$f1 < $f2" else "$f1 = $f2"
                _steps.value = "比較過程：將兩分數通分或轉為小數：\n$f1 ≈ ${String.format("%.3f", val1)}\n$f2 ≈ ${String.format("%.3f", val2)}"
            }
        }
    }

    private fun createFraction(n: String, d: String): Fraction? {
        return try {
            val num = n.toLong(); val den = d.toLong()
            if (den == 0L) null else Fraction(num, den)
        } catch (e: Exception) { null }
    }
}
