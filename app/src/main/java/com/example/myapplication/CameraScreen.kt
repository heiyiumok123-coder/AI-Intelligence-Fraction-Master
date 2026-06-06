package com.example.myapplication

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@Composable
fun CameraScreen(onResultDetected: (OcrUtils.FractionEquation) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    // 使用 remember 並加上 key，確保在 context 或 lifecycle 改變時正確重置
    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(0x2) // IMAGE_ANALYSIS
        }
    }

    // 顯式地綁定生命週期，並在 disposed 時解綁
    DisposableEffect(lifecycleOwner) {
        try {
            cameraController.bindToLifecycle(lifecycleOwner)
        } catch (e: Exception) {
            Log.e("Camera", "Binding error", e)
        }
        onDispose {
            cameraController.unbind()
        }
    }

    var detectedText by remember { mutableStateOf("相機初始化中...") }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 啟動分析器
        LaunchedEffect(cameraController) {
            cameraController.setImageAnalysisAnalyzer(executor) { imageProxy ->
                try {
                    processImageProxy(recognizer, imageProxy) { text ->
                        if (text.isNotBlank()) {
                            detectedText = text
                            val equation = OcrUtils.parseEquation(text)
                            if (equation != null) {
                                onResultDetected(equation)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Camera", "NPE or Analysis error", e)
                    imageProxy.close()
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "辨識中: $detectedText", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) { Text("關閉相機") }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    imageProxy: ImageProxy,
    onTextDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onTextDetected(visionText.text)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
