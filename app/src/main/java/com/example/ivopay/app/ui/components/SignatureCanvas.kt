package com.example.ivopay.app.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignatureCanvas(
    onClear: () -> Unit,
    onSubmit: (Bitmap) -> Unit
) {
    val path = remember { Path() }
    var drawTrigger by remember { mutableIntStateOf(0) }

    // Simpan ukuran Canvas untuk konversi ke Bitmap
    var canvasWidth by remember { mutableIntStateOf(0) }
    var canvasHeight by remember { mutableIntStateOf(0) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8F8F8))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            path.moveTo(offset.x, offset.y)
                            drawTrigger++
                        },
                        onDrag = { change, _ ->
                            path.lineTo(change.position.x, change.position.y)
                            drawTrigger++
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                canvasWidth = size.width.toInt()
                canvasHeight = size.height.toInt()

                // Redraw canvas
                drawTrigger.let {
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    path.reset()
                    drawTrigger++
                    onClear()
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ubah", color = Color(0xFF262626))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (canvasWidth > 0 && canvasHeight > 0) {
                        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                        val androidCanvas = android.graphics.Canvas(bitmap)
                        androidCanvas.drawColor(android.graphics.Color.WHITE)

                        val paint = Paint().apply {
                            color = android.graphics.Color.BLACK
                            strokeWidth = 6f
                            style = Paint.Style.STROKE
                            isAntiAlias = true
                        }
                        androidCanvas.drawPath(path.asAndroidPath(), paint)
                        onSubmit(bitmap)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tandatangani", color = Color.White)
            }
        }
    }
}