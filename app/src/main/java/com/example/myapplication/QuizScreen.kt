package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class QuizQuestion(
    val n1: Long, val d1: Long,
    val n2: Long, val d2: Long,
    val op: String,
    val correctAnswer: Fraction
)

@Composable
fun QuizScreen(onBack: () -> Unit) {
    var maxVal by remember { mutableStateOf("10") }
    var questionCount by remember { mutableStateOf("5") }
    var isStarted by remember { mutableStateOf(false) }
    var currentQuestions by remember { mutableStateOf(listOf<QuizQuestion>()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var userNum by remember { mutableStateOf("") }
    var userDen by remember { mutableStateOf("") }
    var score by remember { mutableIntStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var quizFinished by remember { mutableStateOf(false) }
    var lastCorrect by remember { mutableStateOf(true) }
    var showSteps by remember { mutableStateOf(false) }

    if (quizFinished) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("測驗結束！", style = MaterialTheme.typography.headlineMedium)
            Text("答對題數：$score / ${currentQuestions.size}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) { Text("返回主畫面") }
        }
    } else if (!isStarted) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("刷題模式設定", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            TextField(maxVal, { maxVal = it }, label = { Text("最大分子/分母") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(8.dp))
            TextField(questionCount, { questionCount = it }, label = { Text("總題數") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                val m = maxVal.toLongOrNull() ?: 10L
                val q = questionCount.toIntOrNull() ?: 5
                currentQuestions = List(q) { generateQuestion(m) }
                isStarted = true
            }, Modifier.fillMaxWidth()) { Text("開始刷題") }
            TextButton(onClick = onBack) { Text("取消") }
        }
    } else {
        val q = currentQuestions[currentIndex]
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("第 ${currentIndex + 1} / ${currentQuestions.size} 題", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(32.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuizFractionDisplay(q.n1, q.d1)
                Text(" ${q.op} ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                QuizFractionDisplay(q.n2, q.d2)
                Text(" = ?", fontSize = 24.sp)
            }

            Spacer(Modifier.height(32.dp))
            
            if (!showResult) {
                Text("請輸入答案 (最簡分數)：")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(userNum, { userNum = it }, Modifier.width(80.dp), placeholder = { Text("分子") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Text(" / ", fontSize = 24.sp)
                    TextField(userDen, { userDen = it }, Modifier.width(80.dp), placeholder = { Text("分母") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    val un = userNum.toLongOrNull() ?: 0L
                    val ud = userDen.toLongOrNull() ?: 1L
                    val userAns = Fraction(un, ud).simplify()
                    lastCorrect = userAns == q.correctAnswer
                    if (lastCorrect) score++
                    showResult = true
                }, Modifier.fillMaxWidth()) { Text("檢查答案") }
            } else {
                Text(if (lastCorrect) "✅ 答對了！" else "❌ 答錯了！", fontSize = 32.sp, color = if (lastCorrect) Color(0xFF4CAF50) else Color.Red)
                Text("正確答案：${q.correctAnswer}", style = MaterialTheme.typography.titleMedium)
                
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showSteps = !showSteps }) { Text(if (showSteps) "隱藏步驟" else "顯示步驟") }
                
                if (showSteps) {
                    val f1 = Fraction(q.n1, q.d1)
                    val f2 = Fraction(q.n2, q.d2)
                    val steps = when(q.op) {
                        "+" -> getAdditionSteps(f1, f2, true)
                        "-" -> getSubtractionSteps(f1, f2, true)
                        else -> "計算過程：($f1) ${q.op} ($f2)"
                    }
                    Card(Modifier.padding(8.dp).fillMaxWidth()) {
                        Text(steps, Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (currentIndex < currentQuestions.size - 1) {
                        currentIndex++
                        userNum = ""; userDen = ""; showResult = false; showSteps = false
                    } else {
                        quizFinished = true
                    }
                }, Modifier.fillMaxWidth()) { Text(if (currentIndex < currentQuestions.size - 1) "下一題" else "查看總分") }
            }
        }
    }
}

@Composable
fun QuizFractionDisplay(n: Long, d: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n.toString(), fontSize = 20.sp)
        Box(Modifier.width(40.dp).height(2.dp).background(MaterialTheme.colorScheme.onSurface))
        Text(d.toString(), fontSize = 20.sp)
    }
}

fun generateQuestion(max: Long): QuizQuestion {
    val ops = listOf("+", "-")
    val op = ops.random()
    val d1 = Random.nextLong(1, max + 1)
    val n1 = Random.nextLong(1, max + 1)
    val d2 = Random.nextLong(1, max + 1)
    val n2 = Random.nextLong(1, max + 1)
    
    val f1 = Fraction(n1, d1)
    val f2 = Fraction(n2, d2)
    val correct = if (op == "+") f1 + f2 else f1 - f2
    
    return QuizQuestion(n1, d1, n2, d2, op, correct.simplify())
}
