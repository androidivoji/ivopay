package com.example.ivopay.app.ui.mine

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ivopay.R
import com.example.ivopay.app.data.model.BorrowerCmeData
import com.example.ivopay.app.ui.components.KtpCameraView
import com.example.ivopay.app.ui.components.OptionItem
import com.example.ivopay.app.ui.components.SelectableField
import com.google.gson.JsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobInfoV2Screen(
    viewModel: JobInfoV2ViewModel,
    onBackClick: () -> Unit,
    onNextClick: (BorrowerCmeData?, JsonObject?, JsonObject?) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // UI states for selection sheet
    var showActionSheet by remember { mutableStateOf(false) }
    var currentSelectionType by remember { mutableStateOf("") }
    var actionSheetTitle by remember { mutableStateOf("") }
    var currentOptions by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    
    var showWorkProofTypePop by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var isSelfieMode by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto bukti kerja", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    val workDocumentList = listOf(
        "1. ID card karyawan",
        "2. Slip gaji ( 1 bulan terakhir )",
        "3. Mutasi gaji ( 1 bulan terakhir )",
        "4. Surat keterangan kerja",
        "5. Foto selfie beserta lingkungan kerja"
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

    // Cascading options (jos/ines)
    fun getNestedOptions(key: String): List<OptionItem> {
        val list = mutableListOf<OptionItem>()
        viewModel.commonParams?.getAsJsonArray(key)?.forEach { element ->
            val obj = element.asJsonObject
            list.add(OptionItem(obj.get("n").asString, obj.get("n").asString))
        }
        return list
    }

    // Logic to open next picker automatically
    fun triggerNextPicker(type: String, selectedItem: OptionItem) {
        val state = viewModel.state
        when (type) {
            "jos_parent" -> {
                // Find child options
                val nested = viewModel.commonParams?.getAsJsonArray("jos")?.find { it.asJsonObject.get("n").asString == selectedItem.key }
                val childArray = nested?.asJsonObject?.getAsJsonArray("jos")
                val options = mutableListOf<OptionItem>()
                childArray?.forEach { el ->
                    val obj = el.asJsonObject
                    options.add(OptionItem(obj.get("d").asString, obj.get("n").asString))
                }
                currentSelectionType = "joi"
                actionSheetTitle = "Jenis Pekerjaan"
                currentOptions = options
                showActionSheet = true
            }
            "joi" -> {
                if (state.iniun.isEmpty()) {
                    currentSelectionType = "ines_parent"
                    actionSheetTitle = "Jenis Industri"
                    currentOptions = getNestedOptions("ines")
                    showActionSheet = true
                }
            }
            "ines_parent" -> {
                val nested = viewModel.commonParams?.getAsJsonArray("ines")?.find { it.asJsonObject.get("n").asString == selectedItem.key }
                val childArray = nested?.asJsonObject?.getAsJsonArray("ines")
                val options = mutableListOf<OptionItem>()
                childArray?.forEach { el ->
                    val obj = el.asJsonObject
                    options.add(OptionItem(obj.get("d").asString, obj.get("n").asString))
                }
                currentSelectionType = "ini"
                actionSheetTitle = "Sektor Ekonomi"
                currentOptions = options
                showActionSheet = true
            }
            "ini" -> {
                if (state.jorkn.isEmpty()) {
                    currentSelectionType = "jork"
                    actionSheetTitle = "Jabatan"
                    currentOptions = getOptions("jork")
                    showActionSheet = true
                }
            }
            "jork" -> {
                if (state.wkdnn.isEmpty()) {
                    currentSelectionType = "wkdns"
                    actionSheetTitle = "Lama Bekerja"
                    currentOptions = getOptions("wkdns")
                    showActionSheet = true
                }
            }
        }
    }

    if (showCamera) {
        KtpCameraView(
            onImageCaptured = { bitmap ->
                viewModel.updateField(viewModel.state.copy(wkptie_bitmap = bitmap))
                showCamera = false
            },
            onClose = { showCamera = false },
            cameraSelector = if (isSelfieMode) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
            isFaceMode = isSelfieMode
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Informasi Pekerjaan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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

                    // 1. Nama Perusahaan
                    OutlinedTextField(
                        value = viewModel.state.con,
                        onValueChange = { viewModel.updateField(viewModel.state.copy(con = it)) },
                        label = { Text("Nama Perusahaan") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // 2. Pendapatan Bulanan
                    OutlinedTextField(
                        value = viewModel.state.syamt,
                        onValueChange = { viewModel.updateField(viewModel.state.copy(syamt = it)) },
                        label = { Text("Pendapatan Bulanan") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 3. Jenis Usaha (Parent of jos)
                    SelectableField(
                        label = "Jenis Usaha", 
                        value = viewModel.state.joiun, 
                        onClick = { 
                            currentSelectionType = "jos_parent"
                            actionSheetTitle = "Jenis Usaha"
                            currentOptions = getNestedOptions("jos")
                            showActionSheet = true
                        }
                    )

                    // 4. Dynamic Fields (showJobField logic)
                    val showJobField = viewModel.state.joiun.isNotEmpty() || viewModel.state.join.isNotEmpty() || viewModel.state.iniun.isNotEmpty() || viewModel.state.inin.isNotEmpty() || viewModel.state.jorkn.isNotEmpty() || viewModel.state.wkdnn.isNotEmpty()
                    
                    if (showJobField) {
                        SelectableField(
                            label = "Jenis Pekerjaan", 
                            value = viewModel.state.join, 
                            onClick = { 
                                currentSelectionType = "jos_parent"
                                actionSheetTitle = "Jenis Usaha"
                                currentOptions = getNestedOptions("jos")
                                showActionSheet = true
                            }
                        )
                        SelectableField(
                            label = "Jenis Industri", 
                            value = viewModel.state.iniun, 
                            onClick = { 
                                currentSelectionType = "ines_parent"
                                actionSheetTitle = "Jenis Industri"
                                currentOptions = getNestedOptions("ines")
                                showActionSheet = true
                            }
                        )
                        SelectableField(
                            label = "Sektor Ekonomi", 
                            value = viewModel.state.inin, 
                            onClick = { 
                                currentSelectionType = "ines_parent"
                                actionSheetTitle = "Jenis Industri"
                                currentOptions = getNestedOptions("ines")
                                showActionSheet = true
                            }
                        )
                        SelectableField(
                            label = "Jabatan", 
                            value = viewModel.state.jorkn, 
                            onClick = { 
                                currentSelectionType = "jork"
                                actionSheetTitle = "Jabatan"
                                currentOptions = getOptions("jork")
                                showActionSheet = true
                            }
                        )
                        SelectableField(
                            label = "Lama Bekerja", 
                            value = viewModel.state.wkdnn, 
                            onClick = { 
                                currentSelectionType = "wkdns"
                                actionSheetTitle = "Lama Bekerja"
                                currentOptions = getOptions("wkdns")
                                showActionSheet = true
                            }
                        )
                    }

                    SelectableField(
                        label = "Alamat kantor", 
                        value = viewModel.state.cstr, 
                        onClick = { /* Readonly summary */ }
                    )

                    OutlinedTextField(
                        value = viewModel.state.cdel,
                        onValueChange = { viewModel.updateField(viewModel.state.copy(cdel = it)) },
                        label = { Text("Alamat Lengkap Perusahaan") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. Bukti Kerja
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { showWorkProofTypePop = true },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (viewModel.state.wkptie_bitmap != null) {
                                Image(
                                    bitmap = viewModel.state.wkptie_bitmap!!.asImageBitmap(),
                                    contentDescription = "Work Proof",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (viewModel.state.wkptie.isNotEmpty()) {
                                AsyncImage(
                                    model = viewModel.state.wkptie,
                                    contentDescription = "Work Proof",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.iv_data_ic_sign)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(painterResource(id = R.drawable.iv_data_ic_sign), contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Text("Upload Bukti Kerja", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFBD0100))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unggah bukti kerja untuk mempercepat verifikasi.", fontSize = 12.sp, color = Color.Gray)
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
                                handleJobOptionSelected(viewModel, currentSelectionType, item)
                                showActionSheet = false
                                triggerNextPicker(currentSelectionType, item)
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }

    // Work Proof Type Popup
    if (showWorkProofTypePop) {
        AlertDialog(
            onDismissRequest = { showWorkProofTypePop = false },
            title = { Text("Bukti Kerja", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    val text = if (viewModel.isWorkProofMandatory) "Diwajibkan mengupload salah satu dokumen:" 
                               else "*Mengunggah bukti kerja dapat mempercepat proses audit:"
                    val color = if (viewModel.isWorkProofMandatory) Color.Black else Color(0xFFD93717)
                    
                    Text(text, fontSize = 13.sp, color = color)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    
                    workDocumentList.forEachIndexed { index, doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateField(viewModel.state.copy(wkptie_yep = (index + 1).toString()))
                                    isSelfieMode = (index == 4) // "Foto selfie beserta lingkungan kerja"
                                    showWorkProofTypePop = false
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(doc, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(painterResource(id = R.drawable.iv_set_right_arrow), contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWorkProofTypePop = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White
        )
    }
}

private fun handleJobOptionSelected(viewModel: JobInfoV2ViewModel, type: String, item: OptionItem) {
    when (type) {
        "jos_parent" -> viewModel.updateField(viewModel.state.copy(joiun = item.value, join = ""))
        "joi" -> viewModel.updateField(viewModel.state.copy(joi = item.key, join = item.value))
        "ines_parent" -> viewModel.updateField(viewModel.state.copy(iniun = item.value, inin = ""))
        "ini" -> viewModel.updateField(viewModel.state.copy(ini = item.key, inin = item.value))
        "jork" -> viewModel.updateField(viewModel.state.copy(jork = item.key, jorkn = item.value))
        "wkdns" -> viewModel.updateField(viewModel.state.copy(wkdn = item.key, wkdnn = item.value))
    }
}
