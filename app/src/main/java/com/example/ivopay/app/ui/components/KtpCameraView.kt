package com.example.ivopay.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    onClose: () -> Unit,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA, // Tambahkan parameter selector
    isFaceMode: Boolean = false // Tambahkan flag mode wajah
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Tetap biarkan permission handling di sini sebagai fallback, tapi pemicu utama di Screen
    var hasCameraPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        ) 
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                android.widget.Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", android.widget.Toast.LENGTH_SHORT).show()
                onClose()
            }
        }
    )

    val previewView = remember { 
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector, // Gunakan parameter selector
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e("KtpCamera", "Use case binding failed", exc)
                }
            }, mainExecutor)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize().onGloballyPositioned {
                    viewSize = it.size
                }
            )

            // Guideline Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                if (isFaceMode) {
                    // Oval Guide for Face Mode
                    val ovalWidth = canvasWidth * 0.7f
                    val ovalHeight = ovalWidth * 1.3f
                    val left = (canvasWidth - ovalWidth) / 2
                    val top = (canvasHeight - ovalHeight) / 2.5f

                    val ovalPath = Path().apply {
                        addOval(androidx.compose.ui.geometry.Rect(left, top, left + ovalWidth, top + ovalHeight))
                    }
                    clipPath(ovalPath, clipOp = ClipOp.Difference) {
                        drawRect(color = Color.Black.copy(alpha = 0.7f))
                    }
                    drawPath(path = ovalPath, color = Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                } else {
                    // Rect Guide for KTP Mode
                    val rectWidth = canvasWidth * 0.9f
                    val rectHeight = rectWidth * (54f / 85f)
                    val left = (canvasWidth - rectWidth) / 2
                    val top = (canvasHeight - rectHeight) / 2
                    val right = left + rectWidth
                    val bottom = top + rectHeight

                    val rectPath = Path().apply {
                        addRoundRect(RoundRect(rect = Rect(left, top, right, bottom), cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())))
                    }
                    clipPath(rectPath, clipOp = ClipOp.Difference) {
                        drawRect(color = Color.Black.copy(alpha = 0.7f))
                    }
                    drawPath(path = rectPath, color = Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                }
            }
        }

        // Close Button
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        if (hasCameraPermission) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)) {
                FloatingActionButton(
                    onClick = {
                        val capture = imageCapture ?: return@FloatingActionButton
                        capture.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image)
                                    val rotatedBitmap = rotateBitmap(bitmap, image.imageInfo.rotationDegrees.toFloat())
                                    image.close()

                                    if (viewSize.width > 0 && viewSize.height > 0) {
                                        val cropped = cropBitmapToGuideline(rotatedBitmap, viewSize)
                                        mainExecutor.execute { onImageCaptured(cropped) }
                                    } else {
                                        mainExecutor.execute { onImageCaptured(rotatedBitmap) }
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

    val scale = Math.max(vw / bw, vh / bh)
    val dw = bw * scale
    val dh = bh * scale
    val ox = (vw - dw) / 2f
    val oy = (vh - dh) / 2f

    val rw = vw * 0.9f
    val rh = rw * (54f / 85f)
    val rl = (vw - rw) / 2f
    val rt = (vh - rh) / 2f

    val marginW = rw * 0.05f 
    val marginH = rh * 0.05f 
    
    val expandedRL = rl - marginW
    val expandedRT = rt - marginH
    val expandedRW = rw + (marginW * 2)
    val expandedRH = rh + (marginH * 2)

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
