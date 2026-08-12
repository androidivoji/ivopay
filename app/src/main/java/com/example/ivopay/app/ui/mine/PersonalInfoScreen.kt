package com.example.ivopay.app.ui.mine

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    viewModel: PersonalInfoV2ViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // UI states for selection sheet
    var showActionSheet by remember { mutableStateOf(false) }
    var currentSelectionType by remember { mutableStateOf("") }
    var actionSheetTitle by remember { mutableStateOf("") }
    var currentOptions by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    // DatePicker for Spouse Birth Date
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            viewModel.updateField(viewModel.state.copy(spabire = date))
        },
        calendar.get(Calendar.YEAR) - 25,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Helper to get options from commonParams
    fun getOptions(key: String): List<OptionItem> {
        val list = mutableListOf<OptionItem>()
        viewModel.commonParams?.getAsJsonArray(key)?.forEach { element ->
            val obj = element.asJsonObject
            list.add(OptionItem(obj.get("k").asString, obj.get("v").asString))
        }
        return list
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Data Pribadi", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                // 1. Informasi Identitas
                OutlinedTextField(
                    value = viewModel.state.moe,
                    onValueChange = { 
                        val filtered = CommonUtils.restrictToLetters(it)
                        viewModel.updateField(viewModel.state.copy(moe = filtered)) 
                    },
                    label = { Text("Nama Ibu Kandung") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Text("*Nama lengkap ibu kandung (sesuai dengan KTP)", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                SelectableField(
                    label = "Agama", 
                    value = viewModel.state.reln, 
                    onClick = { 
                        currentSelectionType = "rel"
                        actionSheetTitle = "Pilih Agama"
                        currentOptions = getOptions("rel")
                        showActionSheet = true
                    }
                )

                val showStatusField = viewModel.state.reln.isNotEmpty() || viewModel.state.masn.isNotEmpty() || viewModel.state.ednn.isNotEmpty() || viewModel.state.fasn.isNotEmpty() || viewModel.state.lidnn.isNotEmpty()
                
                if (showStatusField) {
                    SelectableField(
                        label = "Pendidikan", 
                        value = viewModel.state.ednn, 
                        onClick = { 
                            currentSelectionType = "edn"
                            actionSheetTitle = "Pilih Pendidikan"
                            currentOptions = getOptions("edn")
                            showActionSheet = true
                        }
                    )
                    SelectableField(
                        label = "Tipe Tempat Tinggal", 
                        value = viewModel.state.liten, 
                        onClick = { 
                            currentSelectionType = "lite"
                            actionSheetTitle = "Pilih Tipe Tempat Tinggal"
                            currentOptions = getOptions("lite")
                            showActionSheet = true
                        }
                    )
                    SelectableField(
                        label = "Lama Menetap", 
                        value = viewModel.state.lidnn, 
                        onClick = { 
                            currentSelectionType = "lidn"
                            actionSheetTitle = "Pilih Lama Menetap"
                            currentOptions = getOptions("lidn")
                            showActionSheet = true
                        }
                    )
                }

                SelectableField(
                    label = "Tujuan Pinjaman", 
                    value = viewModel.state.lopen, 
                    onClick = { 
                        currentSelectionType = "lope"
                        actionSheetTitle = "Pilih Tujuan Pinjaman"
                        currentOptions = getOptions("lope")
                        showActionSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Alamat Lengkap", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                
                OutlinedTextField(
                    value = viewModel.state.lvstr,
                    onValueChange = {},
                    label = { Text("Alamat tempat tinggal / domisili") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    readOnly = true,
                    enabled = false,
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = viewModel.state.del,
                    onValueChange = { viewModel.updateField(viewModel.state.copy(del = it)) },
                    label = { Text("Alamat Lengkap Detail") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                // 2. Rekening Bank
                Spacer(modifier = Modifier.height(24.dp))
                Text("Rekening Kartu Bank", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                SelectableField(
                    label = "Nama Bank", 
                    value = viewModel.state.ban, 
                    onClick = { 
                        currentSelectionType = "bank"
                        actionSheetTitle = "Pilih Bank"
                        // Filter by ais == 1 as per Vue logic
                        currentOptions = viewModel.commonBankList
                            .filter { it.ais == 1 }
                            .map { OptionItem(it.name ?: "", it.fullName ?: "") }
                        showActionSheet = true
                    }
                )

                OutlinedTextField(
                    value = viewModel.state.bant,
                    onValueChange = { 
                        val filtered = CommonUtils.restrictToNumbers(it)
                        if (filtered.length <= viewModel.bankMaxLength) {
                            viewModel.updateField(viewModel.state.copy(bant = filtered))
                        }
                    },
                    label = { Text("Nomor Rekening") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = viewModel.state.bante,
                    onValueChange = { 
                        val filtered = CommonUtils.restrictToLetters(it)
                        viewModel.updateField(viewModel.state.copy(bante = filtered)) 
                    },
                    label = { Text("Nama Pemilik Rekening") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                // 3. Informasi Keluarga
                Spacer(modifier = Modifier.height(24.dp))
                Text("Informasi Keluarga", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                SelectableField(
                    label = "Status Pernikahan", 
                    value = viewModel.state.masn, 
                    onClick = { 
                        currentSelectionType = "mas"
                        actionSheetTitle = "Status Pernikahan"
                        currentOptions = getOptions("mas")
                        showActionSheet = true
                    }
                )

                if (viewModel.state.mas == "2") {
                    SelectableField(
                        label = "Jumlah Tanggungan", 
                        value = viewModel.state.fasn, 
                        onClick = { 
                            currentSelectionType = "fas"
                            actionSheetTitle = "Jumlah Tanggungan"
                            currentOptions = getOptions("fas")
                            showActionSheet = true
                        }
                    )
                    
                    OutlinedTextField(
                        value = viewModel.state.spane,
                        onValueChange = { 
                            val filtered = CommonUtils.restrictToLetters(it)
                            viewModel.updateField(viewModel.state.copy(spane = filtered)) 
                        },
                        label = { Text("Nama Lengkap Pasangan") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    SelectableField(
                        label = "Tgl Lahir Pasangan", 
                        value = viewModel.state.spabire, 
                        onClick = { datePickerDialog.show() }
                    )
                    SelectableField(
                        label = "Perjanjian Pisah Harta", 
                        value = viewModel.state.happtyagmetne, 
                        onClick = { 
                            currentSelectionType = "happtyagmet"
                            actionSheetTitle = "Perjanjian Pisah Harta"
                            currentOptions = getOptions("happtyagmet")
                            showActionSheet = true
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
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sebelumnya", color = Color.Gray)
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
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Selanjutnya")
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for pickers
    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = actionSheetTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)) {
                    items(currentOptions) { item ->
                        ListItem(
                            headlineContent = { Text(item.value) },
                            modifier = Modifier.clickable {
                                handleOptionSelected(viewModel, currentSelectionType, item)
                                showActionSheet = false
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

private fun handleOptionSelected(viewModel: PersonalInfoV2ViewModel, type: String, item: OptionItem) {
    when (type) {
        "rel" -> viewModel.updateField(viewModel.state.copy(rel = item.key, reln = item.value))
        "edn" -> viewModel.updateField(viewModel.state.copy(edn = item.key, ednn = item.value))
        "lite" -> viewModel.updateField(viewModel.state.copy(lite = item.key, liten = item.value))
        "lidn" -> viewModel.updateField(viewModel.state.copy(lidn = item.key, lidnn = item.value))
        "lope" -> viewModel.updateField(viewModel.state.copy(lope = item.key, lopen = item.value))
        "bank" -> viewModel.bankChange(item.key) 
        "mas" -> viewModel.updateField(viewModel.state.copy(mas = item.key, masn = item.value))
        "fas" -> {
             if (viewModel.state.mas == "2" && item.value == "Tidak Ada") {
                 // Sesuai logika Vue: minimal 1 orang untuk kawin
                 return
             }
             viewModel.updateField(viewModel.state.copy(fas = item.key, fasn = item.value))
        }
        "happtyagmet" -> viewModel.updateField(viewModel.state.copy(happtyagmet = item.key, happtyagmetne = item.value))
    }
}
