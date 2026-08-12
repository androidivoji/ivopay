package com.example.ivopay.app.ui.mine

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import coil.compose.AsyncImage
import com.example.ivopay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseInfoScreen(
    viewModel: BaseInfoViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showAddressSheet by remember { mutableStateOf(false) }
    var addressMode by remember { mutableStateOf("l") } // "l" for local, "c" for company
    
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Informasi Dasar", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(bottom = 80.dp)
            ) {
                // 1. KTP Photo Section
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp).clickable { 
                        // Simulate taking photo
                        Toast.makeText(context, "Membuka Kamera...", Toast.LENGTH_SHORT).show()
                    },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (viewModel.showOCRLoading) {
                            CircularProgressIndicator(color = Color(0xFFBD0100))
                        } else {
                            if (viewModel.state.ktpUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = viewModel.state.ktpUrl,
                                    contentDescription = "KTP Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds,
                                    placeholder = painterResource(id = R.drawable.iv_data_ic_sign),
                                    error = painterResource(id = R.drawable.iv_data_ic_sign)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(painterResource(id = R.drawable.iv_data_ic_sign), contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Text("Scan Sisi Depan KTP", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                if (viewModel.ocrAts == 2) {
                    StatusTip(text = "NIK & Nama berhasil dikenali", color = Color(0xFFE6FFFA), icon = Icons.Default.Info)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Input Fields
                OutlinedTextField(
                    value = viewModel.state.inm,
                    onValueChange = { if (it.length <= 16) viewModel.updateField(viewModel.state.copy(inm = it)) },
                    label = { Text("Nomor NIK KTP") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.state.funName,
                    onValueChange = { viewModel.updateField(viewModel.state.copy(funName = it)) },
                    label = { Text("Nama Lengkap (Sesuai KTP)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                // 3. Selection Fields (Simulated Click to Open List)
                SelectionField(
                    label = "Jenis Kelamin",
                    value = if (viewModel.state.gen == 1) "Laki-laki" else if (viewModel.state.gen == 2) "Perempuan" else "",
                    onClick = { /* Show Gender Dialog */ }
                )

                SelectionField(
                    label = "Tanggal Lahir",
                    value = viewModel.state.bire,
                    onClick = { /* Show Date Picker */ }
                )

                SelectionField(
                    label = "Alamat Domisili",
                    value = viewModel.state.lvstr,
                    onClick = { 
                        addressMode = "l"
                        viewModel.loadAddresses(1, "0")
                        showAddressSheet = true 
                    }
                )

                SelectionField(
                    label = "Alamat Kantor",
                    value = viewModel.state.cstr,
                    onClick = { 
                        addressMode = "c"
                        viewModel.loadAddresses(1, "0")
                        showAddressSheet = true 
                    }
                )

                OutlinedTextField(
                    value = viewModel.state.eil,
                    onValueChange = { viewModel.updateField(viewModel.state.copy(eil = it)) },
                    label = { Text("Email (Gmail)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Bottom Action Bar
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sebelumnya", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            viewModel.submitInfo(
                                onSuccess = onNextClick,
                                onError = { msg, code -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Selanjutnya")
                    }
                }
            }
        }
    }

    // Address Picker Bottom Sheet
    if (showAddressSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddressSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = "Pilih ${viewModel.addressPickerTitle}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(viewModel.addressList) { item ->
                        ListItem(
                            headlineContent = { Text(item.name ?: "") },
                            modifier = Modifier.clickable {
                                // Logic for cascading address
                                if (viewModel.currentAddressLevel < 4) {
                                    viewModel.loadAddresses(viewModel.currentAddressLevel + 1, item.code ?: "")
                                } else {
                                    // Final selection logic to update lvstr/cstr
                                    if (addressMode == "l") {
                                        viewModel.updateField(viewModel.state.copy(lvstr = (viewModel.state.lvstr + " " + item.name).trim()))
                                    } else {
                                        viewModel.updateField(viewModel.state.copy(cstr = (viewModel.state.cstr + " " + item.name).trim()))
                                    }
                                    showAddressSheet = false
                                }
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionField(label: String, value: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable { onClick() }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline
            ),
            trailingIcon = { Icon(painterResource(id = R.drawable.iv_set_right_arrow), contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
    }
}

@Composable
fun StatusTip(text: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).background(color, RoundedCornerShape(4.dp)).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00B95E))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = Color(0xFF00B95E))
    }
}
