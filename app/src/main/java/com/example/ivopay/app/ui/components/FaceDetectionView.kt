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
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun FaceDetectionView(
    onImageCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = ContextCompat.getMainExecutor(context) // Executor untuk utas utama
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var hasCameraPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        ) 
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                android.widget.Toast.makeText(context, "Izin kamera ditolak", android.widget.Toast.LENGTH_SHORT).show()
                onClose()
            }
        }
    )

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val previewView = remember { 
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
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

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    Log.d("FaceDetect", "Camera bound successfully")
                } catch (exc: Exception) {
                    Log.e("FaceDetect", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
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
                modifier = Modifier.fillMaxSize()
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
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
                
                drawPath(
                    path = ovalPath,
                    color = Color.White,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Verifikasi Wajah", color = Color.White, fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(text = "Posisikan wajah Anda di dalam area oval", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        if (hasCameraPermission) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)) {
                FloatingActionButton(
                    onClick = {
                        val capture = imageCapture ?: return@FloatingActionButton
                        capture.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image)
                                    val matrix = Matrix()
                                    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
                                    matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                                    val finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                    image.close()
                                    
                                    // Jalankan callback di Utas Utama (Main Thread) untuk mencegah crash navigasi
                                    mainExecutor.execute {
                                        onImageCaptured(finalBitmap)
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("FaceDetect", "Capture failed: ${exception.message}")
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
