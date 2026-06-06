package com.example.myapplication

/**
 * 負責將 OCR 辨識出的字串解析為分數運算
 * 例如將 "1/2 + 1/3" 解析為 Fraction(1,2), Fraction(1,3) 與 "+" 運算子
 */
object OcrUtils {
    data class FractionEquation(
        val n1: Int, val d1: Int,
        val n2: Int, val d2: Int,
        val operator: String
    )

    fun parseEquation(text: String): FractionEquation? {
        // 清理字串，將常見 OCR 錯誤替換回標準字元
        val cleanText = text.replace(" ", "")
            .replace("|", "/")
            .replace("x", "*")
            .replace("÷", "/")
        
        // 正規表示式：匹配 (數字/數字) (運算子) (數字/數字)
        // 支持的格式如 1/2+3/4
        val regex = Regex("""(\d+)/(\d+)([\+\-\*\/])(\d+)/(\d+)""")
        val match = regex.find(cleanText) ?: return null

        return try {
            val groups = match.groupValues
            FractionEquation(
                n1 = groups[1].toInt(),
                d1 = groups[2].toInt(),
                operator = groups[3],
                n2 = groups[4].toInt(),
                d2 = groups[5].toInt()
            )
        } catch (e: Exception) {
            null
        }
    }
}
