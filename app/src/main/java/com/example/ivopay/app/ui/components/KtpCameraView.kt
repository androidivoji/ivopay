package com.example.ivopay.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun KtpCameraView(
    onImageCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e("KtpCamera", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize().onGloballyPositioned {
                viewSize = it.size
            }
        )

        // 2. Guideline Overlay (Rasio 85:54)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val rectWidth = canvasWidth * 0.9f
            val rectHeight = rectWidth * (54f / 85f)
            
            val left = (canvasWidth - rectWidth) / 2
            val top = (canvasHeight - rectHeight) / 2
            val right = left + rectWidth
            val bottom = top + rectHeight

            val rectPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, right, bottom),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                )
            }

            clipPath(rectPath, clipOp = ClipOp.Difference) {
                drawRect(color = Color.Black.copy(alpha = 0.7f))
            }
            
            drawPath(
                path = rectPath,
                color = Color.White,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }

        // 3. Buttons
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)) {
            FloatingActionButton(
                onClick = {
                    val capture = imageCapture ?: return@FloatingActionButton
                    capture.takePicture(
                        cameraExecutor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = imageProxyToBitmap(image)
                                // ROTASI SANGAT PENTING: Menyelaraskan orientasi sensor dengan layar HP
                                val rotatedBitmap = rotateBitmap(bitmap, image.imageInfo.rotationDegrees.toFloat())
                                image.close()

                                // Proses Cropping yang akurat sesuai Guideline di layar
                                if (viewSize.width > 0 && viewSize.height > 0) {
                                    val cropped = cropBitmapToGuideline(rotatedBitmap, viewSize)
                                    onImageCaptured(cropped)
                                } else {
                                    onImageCaptured(rotatedBitmap)
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("KtpCamera", "Capture failed: ${exception.message}")
                            }
                        }
                    )
                },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Shutter", modifier = Modifier.size(36.dp))
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    if (angle == 0f) return source
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun cropBitmapToGuideline(bitmap: Bitmap, viewSize: IntSize): Bitmap {
    val bw = bitmap.width.toFloat()
    val bh = bitmap.height.toFloat()
    val vw = viewSize.width.toFloat()
    val vh = viewSize.height.toFloat()

    // Hitung skala PreviewView (FILL_CENTER)
    val scale = Math.max(vw / bw, vh / bh)
    val dw = bw * scale
    val dh = bh * scale
    val ox = (vw - dw) / 2f
    val oy = (vh - dh) / 2f

    // Koordinat Guideline di layar
    val rw = vw * 0.9f
    val rh = rw * (54f / 85f)
    val rl = (vw - rw) / 2f
    val rt = (vh - rh) / 2f

    // TAMBAHKAN MARGIN AMAN (10% lebih luas agar tidak terlalu mepet)
    val marginW = rw * 0.05f // 5% kiri + 5% kanan
    val marginH = rh * 0.05f // 5% atas + 5% bawah
    
    val expandedRL = rl - marginW
    val expandedRT = rt - marginH
    val expandedRW = rw + (marginW * 2)
    val expandedRH = rh + (marginH * 2)

    // Konversi koordinat layar ke koordinat piksel Bitmap asli
    val cropL = ((expandedRL - ox) / scale).toInt().coerceIn(0, bitmap.width - 1)
    val cropT = ((expandedRT - oy) / scale).toInt().coerceIn(0, bitmap.height - 1)
    val cropW = (expandedRW / scale).toInt().coerceAtMost(bitmap.width - cropL)
    val cropH = (expandedRH / scale).toInt().coerceAtMost(bitmap.height - cropT)

    return try {
        Bitmap.createBitmap(bitmap, cropL, cropT, cropW, cropH)
    } catch (e: Exception) {
        bitmap
    }
}
