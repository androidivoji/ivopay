package com.example.ivopay.app.ui.lender.portofolio.waitsignature

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import java.text.NumberFormat
import java.util.Locale

private fun Double.toRp(): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0
    return format.format(this)
}

@Composable
fun WaitSignatureScreen(
    viewModel: WaitSignatureViewModel = remember { WaitSignatureViewModel() },
    onUpdateCount: (Int) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getContracts(onUpdateCount)
    }

    // Handle Toast
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFB12127)
            )
        } else if (uiState.contractLists.isEmpty()) {
            EmptyStateView()
        } else {
            // Content List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                items(uiState.contractLists, key = { it.odi }) { item ->
                    WaitSignItemCard(
                        item = item,
                        onToggleSelect = { viewModel.toggleSelectItem(item.odi) },
                        onJumpDetail = { onNavigateToDetail(item.odi) }
                    )
                }
            }

            // Fixed Sticky Bottom Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { viewModel.prepareBatchSign() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB12127)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Batch to sign",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Popup / Dialog Sign All
        if (uiState.showSignAllModal) {
            SignAllDialog(
                borrowerNames = viewModel.getSelectedBorrowerNames(),
                isChecked = uiState.isAgreementChecked,
                onToggleCheck = { viewModel.toggleAgreementCheck() },
                onDismiss = { viewModel.dismissSignModal() },
                onConfirmSign = { viewModel.executeSignItems() }
            )
        }

        // Popup / Dialog Progress Bar
        if (uiState.showSignProgressModal) {
            SignProgressDialog(progress = uiState.signProgressPercent)
        }
    }
}

@Composable
fun WaitSignItemCard(
    item: WaitSignOrderItem,
    onToggleSelect: () -> Unit,
    onJumpDetail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            // Top Section (Gradient Merah + Info + Custom Checkbox)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFB12127), Color(0xFFEB6767))
                        )
                    )
                    .clickable { onToggleSelect() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CardRowInfo(label = "Order", value = item.sno)
                        CardRowInfo(label = "Amount", value = item.toa.toString())
                        CardRowInfo(label = "Total", value = item.tpa.toRp())
                    }

                    // Selection Icon/Checkbox
                    Spacer(modifier = Modifier.width(16.dp))
                    Image(
                        painter = painterResource(
                            id = if (item.isSelect) R.drawable.iv_choose_sel else R.drawable.iv_choose_nor
                        ),
                        contentDescription = "Select Order",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Navigation Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJumpDetail() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iv_invest_logo),
                    contentDescription = "Invest Logo",
                    modifier = Modifier.width(55.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.iv_set_right_arrow),
                    contentDescription = "Next Arrow",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SignAllDialog(
    borrowerNames: List<String>,
    isChecked: Boolean,
    onToggleCheck: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmSign: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign All Agreements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262626)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List Borrower Names
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    borrowerNames.forEachIndexed { index, name ->
                        Text(
                            text = "${index + 1}. $name",
                            fontSize = 13.sp,
                            color = Color(0xFF595959)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox syarat & ketentuan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleCheck() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isChecked) R.drawable.iv_choose2_sel else R.drawable.iv_choose2_nor
                        ),
                        contentDescription = "Checkbox Agreement",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I have read and understand each agreement in its entirety",
                        fontSize = 12.sp,
                        color = Color(0xFF8C8C8C)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onConfirmSign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB12127)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(text = "Sign All", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SignProgressDialog(progress: Float) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agreement signing progress bar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262626)
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFBD0100),
                    trackColor = Color(0xFFF2F2F2)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Agreement is being signed, please do not close the page",
                    fontSize = 12.sp,
                    color = Color(0xFF8C8C8C)
                )
            }
        }
    }
}

@Composable
private fun CardRowInfo(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", fontSize = 13.sp, color = Color.White)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.iv_apply_empty_state),
            contentDescription = "Empty State",
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Data tidak ditemukan",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}