package com.example.ivopay.app.ui.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureCreateScreen(
    viewModel: GestureCreateViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Atur Pola Kunci", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = viewModel.infoTips,
                color = if (viewModel.isTipsError) Color.Red else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Reuse GestureLockView from GestureLoginScreen
            GestureLockView(
                onPatternComplete = { pattern ->
                    viewModel.handlePatternComplete(pattern) { successMsg ->
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(1000)
                            onSuccess()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Atur Ulang",
                color = Color(0xFFBD0100),
                fontSize = 14.sp,
                modifier = Modifier.clickable { viewModel.reset() }
            )
        }

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFBD0100))
            }
        }
    }
}
