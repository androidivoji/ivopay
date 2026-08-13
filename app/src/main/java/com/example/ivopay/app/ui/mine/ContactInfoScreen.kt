package com.example.ivopay.app.ui.mine

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.OptionItem
import com.example.ivopay.app.ui.components.SelectableField
import com.example.ivopay.app.util.CommonUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(
    viewModel: ContactInfoViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var showRelationSheet by remember { mutableStateOf(false) }
    var selectedContactIndex by remember { mutableIntStateOf(0) }
    
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Informasi Kontak", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 100.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                viewModel.contacts.forEachIndexed { index, contact ->
                    Text(
                        text = "Kontak Darurat ${index + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF262626),
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // 1. Phone Input
                    OutlinedTextField(
                        value = contact.phe,
                        onValueChange = { 
                            var filtered = it.filter { char -> char.isDigit() }
                            if (filtered.startsWith("0")) filtered = filtered.substring(1)
                            if (filtered.length <= 12) {
                                viewModel.updateContact(index, contact.copy(phe = filtered))
                            }
                        },
                        label = { Text("Nomor Telepon") },
                        prefix = { Text("+62 ", fontWeight = FontWeight.Bold, color = Color(0xFF262626)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 2. Name Input
                    OutlinedTextField(
                        value = contact.funName,
                        onValueChange = { 
                            val filtered = CommonUtils.restrictToLetters(it)
                            viewModel.updateContact(index, contact.copy(funName = filtered)) 
                        },
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // 3. Relation Selection
                    SelectableField(
                        label = "Hubungan", 
                        value = contact.reln, 
                        onClick = { 
                            selectedContactIndex = index
                            showRelationSheet = true
                        }
                    )
                }
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
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Sebelumnya", color = Color(0xFF262626))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            viewModel.submitInfo(
                                onSuccess = onNextClick,
                                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Selanjutnya")
                    }
                }
            }
        }
    }

    // Relation Picker Bottom Sheet
    if (showRelationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRelationSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = "Pilih Hubungan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)) {
                    items(viewModel.relationOptions) { item ->
                        ListItem(
                            headlineContent = { Text(item.value) },
                            modifier = Modifier.clickable {
                                val current = viewModel.contacts[selectedContactIndex]
                                viewModel.updateContact(selectedContactIndex, current.copy(rel = item.key, reln = item.value))
                                showRelationSheet = false
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}
