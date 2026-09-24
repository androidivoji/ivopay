package com.example.ivopay.app.ui.loan

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireViewModel,
    rasn: String,
    onBack: () -> Unit,
    onNavigateToJmo: (String) -> Unit,
    onNavigateToCi10: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(rasn) {
        viewModel.init(rasn)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // 1. Banner
                Image(
                    painter = painterResource(id = R.drawable.iv_offline_loan_qstnr_banner), // Placeholder for banner
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(180.dp),
                    contentScale = ContentScale.FillWidth
                )

                // 2. Questions
                if (viewModel.showFirstQuestion) {
                    QuestionSection(
                        question = "1. Boleh kami tahu kepemilikan aset tetap utama Anda saat ini (seperti rumah atau mobil)?",
                        options = viewModel.selectList1,
                        selectedOption = viewModel.checked,
                        onOptionSelected = { viewModel.checked = it }
                    )
                }

                if (viewModel.showSecondQuestion) {
                    QuestionSection(
                        question = "2. Mohon pilih kondisi pinjaman atas aset tetap Anda (rumah atau mobil)?",
                        options = viewModel.selectList2,
                        selectedOption = viewModel.checked2,
                        onOptionSelected = { viewModel.checked2 = it }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Submit Button
                Button(
                    onClick = {
                        viewModel.onClickSubmit(
                            onSuccess = { nbj ->
                                if (nbj) onNavigateToJmo(viewModel.rasn)
                                else onNavigateToCi10(viewModel.rasn)
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                    shape = RoundedCornerShape(24.dp),
                    enabled = (viewModel.showFirstQuestion && viewModel.checked.isNotEmpty()) || 
                              (viewModel.showSecondQuestion && viewModel.checked2.isNotEmpty())
                ) {
                    Text("Ajukan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }
        }
    }
}

@Composable
fun QuestionSection(
    question: String,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = question, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF262626))
        
        Spacer(modifier = Modifier.height(12.dp))
        
        options.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(value) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (selectedOption == value) R.drawable.iv_choose_sel else R.drawable.iv_choose_nor
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = label, fontSize = 14.sp, color = Color(0xFF666666))
            }
        }
    }
}
