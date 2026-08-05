package com.example.ivopay.app.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.ui.navigation.Screen
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureLoginScreen(
    viewModel: GestureLoginViewModel,
    onNavigate: (String) -> Unit,
    onNavigateBackToLogin: (reset: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.iv_logo_ivoji_splash),
                contentDescription = "Logo",
                modifier = Modifier.width(240.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Phone Number with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.iv_borrower_ic_name),
                    contentDescription = "Hi Icon",
                    modifier = Modifier.size(26.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.phoneNumber,
                    fontSize = 16.sp,
                    color = Color(0xFF262626)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Border Box with Info Tips and Gesture Lock
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (viewModel.infoTips.isNotEmpty()) {
                        Text(
                            text = viewModel.infoTips,
                            color = if (viewModel.isTipsError) Color.Red else Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Gesture Lock View
                    GestureLockView(
                        onPatternComplete = { pattern ->
                            viewModel.requestGestureLogin(pattern, onSuccess = onNavigate, onError = {})
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Entry
            Text(
                text = "Gunakan Metode Login Lain",
                color = Color(0xFFBD0100),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .clickable { viewModel.showLoginMethodPop = true }
            )
        }

        // Action Sheet
        if (viewModel.showLoginMethodPop) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.showLoginMethodPop = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                    ListItem(
                        headlineContent = { Text("Masuk dengan Kode Verifikasi", color = Color(0xFFBD0100)) },
                        modifier = Modifier.clickable {
                            viewModel.showLoginMethodPop = false
                            onNavigateBackToLogin(false)
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Masuk dengan Akun Lain", color = Color(0xFFBD0100)) },
                        modifier = Modifier.clickable {
                            viewModel.showLoginMethodPop = false
                            onNavigateBackToLogin(true)
                        }
                    )
                    ListItem(
                        headlineContent = { 
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Batal", color = Color.Gray)
                            }
                        },
                        modifier = Modifier.clickable { viewModel.showLoginMethodPop = false }
                    )
                }
            }
        }

        // Recovery Popup
        if (viewModel.showLoginTipPop) {
            Dialog(onDismissRequest = { viewModel.showLoginTipPop = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saran yang baik",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191919)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Terdeteksi jika Anda telah mengajukan permohonan pembatalan akun, silahkan login ke akun Anda dan langsung pulihkan akun Anda.",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "KTP：${viewModel.inmText}",
                            fontSize = 14.sp,
                            color = Color(0xFF191919),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.showLoginTipPop = false
                                viewModel.determineNextRoute(onNavigate)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Konfirmasi", color = Color.White)
                        }
                    }
                }
            }
        }

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFBD0100))
            }
        }
    }
}

@Composable
fun GestureLockView(
    onPatternComplete: (String) -> Unit
) {
    var selectedDots by remember { mutableStateOf(listOf<Int>()) }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    val density = LocalDensity.current
    val dotRadius = 8.dp
    val outerRadius = 30.dp
    val spacing = 80.dp

    Box(
        modifier = Modifier
            .size(300.dp)
            .pointerInput(Unit) {
                val spacingPx = with(density) { spacing.toPx() }
                val canvasSizePx = with(density) { 300.dp.toPx() }
                val centerPx = canvasSizePx / 2
                val startXPx = centerPx - spacingPx
                val startYPx = centerPx - spacingPx
                val hitRadius = with(density) { outerRadius.toPx() }

                detectDragGestures(
                    onDragStart = { offset ->
                        selectedDots = emptyList()
                        currentTouchPoint = offset
                        checkIntersection(offset, startXPx, startYPx, spacingPx, hitRadius)?.let { 
                            selectedDots = selectedDots + it 
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newPoint = change.position
                        currentTouchPoint = newPoint
                        checkIntersection(newPoint, startXPx, startYPx, spacingPx, hitRadius)?.let { dot ->
                            if (dot !in selectedDots) {
                                selectedDots = selectedDots + dot
                            }
                        }
                    },
                    onDragEnd = {
                        if (selectedDots.isNotEmpty()) {
                            onPatternComplete(selectedDots.joinToString(""))
                        }
                        selectedDots = emptyList()
                        currentTouchPoint = null
                    },
                    onDragCancel = {
                        selectedDots = emptyList()
                        currentTouchPoint = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacingPx = spacing.toPx()
            val startXPx = size.width / 2 - spacingPx
            val startYPx = size.height / 2 - spacingPx

            for (i in 0..8) {
                val p = getDotOffset(i, startXPx, startYPx, spacingPx)
                val isSelected = i in selectedDots
                val dotColor = if (isSelected) Color(0xFFBD0100) else Color(0xFFE0E0E0)
                
                drawCircle(color = dotColor, radius = dotRadius.toPx(), center = p)
                if (isSelected) {
                    drawCircle(color = dotColor.copy(alpha = 0.2f), radius = outerRadius.toPx(), center = p)
                }
            }

            if (selectedDots.isNotEmpty()) {
                for (i in 0 until selectedDots.size - 1) {
                    val p1 = getDotOffset(selectedDots[i], startXPx, startYPx, spacingPx)
                    val p2 = getDotOffset(selectedDots[i+1], startXPx, startYPx, spacingPx)
                    drawLine(color = Color(0xFFBD0100), start = p1, end = p2, strokeWidth = 4.dp.toPx())
                }
                currentTouchPoint?.let { touchPoint ->
                    val lastOffset = getDotOffset(selectedDots.last(), startXPx, startYPx, spacingPx)
                    drawLine(color = Color(0xFFBD0100), start = lastOffset, end = touchPoint, strokeWidth = 4.dp.toPx())
                }
            }
        }
    }
}

private fun getDotOffset(index: Int, startX: Float, startY: Float, spacing: Float): Offset {
    val row = index / 3
    val col = index % 3
    return Offset(startX + col * spacing, startY + row * spacing)
}

private fun checkIntersection(point: Offset, startX: Float, startY: Float, spacing: Float, hitRadius: Float): Int? {
    for (i in 0..8) {
        val row = i / 3
        val col = i % 3
        val dotX = startX + col * spacing
        val dotY = startY + row * spacing
        val distance = sqrt((point.x - dotX) * (point.x - dotX) + (point.y - dotY) * (point.y - dotY))
        if (distance < hitRadius) return i
    }
    return null
}
