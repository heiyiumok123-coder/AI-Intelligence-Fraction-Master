package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.*

@Composable
fun FractionScreen(viewModel: FractionViewModel = viewModel()) {
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isQuizMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAurora()

        if (isAdminMode) {
            AdminControlPanel(viewModel, onExit = { viewModel.exitAdmin() })
        } else if (isQuizMode) {
            QuizScreen(onBack = { isQuizMode = false })
        } else {
            MainMathApp(viewModel, 
                onAdminClick = { showPasswordDialog = true },
                onQuizClick = { isQuizMode = true }
            )
        }

        if (showPasswordDialog) {
            PasswordEntryDialog(onDismiss = { showPasswordDialog = false }, onVerify = { if (viewModel.tryLogin(it)) showPasswordDialog = false })
        }
    }
}

@Composable
fun BackgroundAurora() {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse), label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.linearGradient(
            colors = listOf(iOSPink, iOSCyan, iOSPurple),
            start = androidx.compose.ui.geometry.Offset(animOffset, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, animOffset)
        )
    ).blur(100.dp))
}

@Composable
fun MainMathApp(viewModel: FractionViewModel, onAdminClick: () -> Unit, onQuizClick: () -> Unit) {
    val context = LocalContext.current
    var n1 by remember { mutableStateOf("") }; var d1 by remember { mutableStateOf("") }
    var n2 by remember { mutableStateOf("") }; var d2 by remember { mutableStateOf("") }
    val result by viewModel.result.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val shouldSimplify by viewModel.shouldSimplify.collectAsState()
    val updateNotice by viewModel.updateNotice.collectAsState()

    var isCameraMode by remember { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) isCameraMode = true }

    // 雲端同步視窗
    updateNotice?.let { info ->
        if (info.isEnabled) {
            AlertDialog(
                onDismissRequest = { if (!info.isForce) viewModel.dismissUpdate() },
                title = { Text(if (info.isForce) "⚠️ 版本強制更新" else "發現新版本: ${info.ver}") },
                text = { Text(info.log) },
                confirmButton = { 
                    Button(onClick = { if (info.url.isNotEmpty()) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.url))) }) { Text("前往下載") } 
                },
                dismissButton = {
                    if (!info.isForce) { TextButton(onClick = { viewModel.dismissUpdate() }) { Text("稍後") } }
                }
            )
        }
    }

    if (isCameraMode) {
        CameraScreen(onResultDetected = { viewModel.onOcrResult(it); isCameraMode = false }, onBack = { isCameraMode = false })
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AI INTELLIGENCE", style = MaterialTheme.typography.labelSmall, letterSpacing = 4.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("分數掃描神器", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) isCameraMode = true 
                    else cameraLauncher.launch(Manifest.permission.CAMERA)
                }, icon = Icons.Default.Add, text = "拍照辨識", modifier = Modifier.weight(1f))
                GlassButton(onClick = onQuizClick, icon = Icons.Default.MenuBook, text = "刷題模式", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            LiquidGlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FractionInputBox(n1, { n1 = it }, "分子 1")
                        Text("與", Modifier.padding(horizontal = 12.dp), color = Color.White)
                        FractionInputBox(n2, { n2 = it }, "分子 2")
                    }
                    Spacer(Modifier.height(12.dp)); Row(verticalAlignment = Alignment.CenterVertically) {
                        FractionInputBox(d1, { d1 = it }, "分母 1"); Spacer(Modifier.width(44.dp)); FractionInputBox(d2, { d2 = it }, "分母 2")
                    }
                    Spacer(Modifier.height(20.dp)); Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("結果化簡", color = Color.White); Switch(shouldSimplify, { viewModel.setShouldSimplify(it) }, Modifier.padding(start = 8.dp))
                    }
                    Spacer(Modifier.height(16.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("+", "-", "×", "÷").forEach { op -> SmallGlassButton(onClick = { viewModel.calculate(n1,d1,n2,d2,op) }, text = op) }
                    }
                    Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallGlassButton(onClick = { viewModel.calculate(n1,d1,n2,d2,"比較") }, text = "比較大小", modifier = Modifier.weight(1f))
                        SmallGlassButton(onClick = { viewModel.quickSimplify(n1, d1) }, text = "直接最簡", modifier = Modifier.weight(1f), color = Color(0xFFBB86FC).copy(alpha = 0.4f))
                    }
                }
            }

            if (result.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                LiquidGlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text(result, style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        if(steps.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp)); Box(Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.2f)))
                            Spacer(Modifier.height(12.dp)); Text(steps, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp)); Icon(Icons.Default.Settings, null, Modifier.size(24.dp).clickable { onAdminClick() }.alpha(0.3f), tint = Color.White)
        }
    }
}

@Composable
fun AdminControlPanel(viewModel: FractionViewModel, onExit: () -> Unit) {
    val cloudStatus by viewModel.cloudStatus.collectAsState()
    var ver by remember { mutableStateOf("1.5") }; var log by remember { mutableStateOf("更新日誌...") }
    var url by remember { mutableStateOf("https://") }; var isForce by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        LiquidGlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("雲端主控台", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(Modifier.height(8.dp)); Text(cloudStatus, color = Color.Yellow, fontSize = 12.sp)
                
                Spacer(Modifier.height(24.dp))
                TextField(ver, { ver = it }, label = { Text("版本號") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp)); TextField(log, { log = it }, label = { Text("內容") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp)); TextField(url, { url = it }, label = { Text("下載連結") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp)); Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("強制更新", color = Color.White); Checkbox(isForce, { isForce = it })
                }
                
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.publishToCloud(ver, log, isForce, url) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("一鍵發布到 Pastebin")
                }
            }
        }
        Spacer(Modifier.height(24.dp)); Button(onClick = onExit, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.5f))) { Text("退出管理員模式") }
    }
}

@Composable
fun LiquidGlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.shadow(20.dp, shape = RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(GlassWhite).border(1.dp, GlassBorder, RoundedCornerShape(28.dp))) { content() }
}

@Composable
fun GlassButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.height(60.dp).border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp)), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)), shape = RoundedCornerShape(18.dp)) {
        Icon(icon, null, Modifier.size(18.dp), tint = Color.White); Spacer(Modifier.width(8.dp)); Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SmallGlassButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier, color: Color = Color.White.copy(alpha = 0.15f)) {
    Button(onClick = onClick, modifier = modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FractionInputBox(v: String, onV: (String) -> Unit, label: String) {
    TextField(value = v, onValueChange = onV, label = { Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f)) }, modifier = Modifier.width(90.dp), singleLine = true, colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

@Composable
fun PasswordEntryDialog(onDismiss: () -> Unit, onVerify: (String) -> Unit) {
    var pass by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("管理員驗證") }, text = { TextField(pass, { pass = it }, label = { Text("密碼") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)) }, confirmButton = { Button(onClick = { onVerify(pass) }) { Text("登入") } })
}
