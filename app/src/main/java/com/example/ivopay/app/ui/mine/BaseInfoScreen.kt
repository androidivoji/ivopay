package com.example.ivopay.app.ui.mine

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.KtpCameraView
import com.example.ivopay.app.ui.components.OptionItem
import com.example.ivopay.app.ui.components.SelectableField
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseInfoScreen(
    viewModel: BaseInfoViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // UI states for selection sheet
    var showActionSheet by remember { mutableStateOf(false) }
    var actionSheetTitle by remember { mutableStateOf("") }
    var currentSelectionType by remember { mutableStateOf("") } 
    var addressMode by remember { mutableStateOf("l") } 
    
    var showCamera by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()

    // Launcher untuk izin kamera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto KTP", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    // DatePicker for Birthday
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
            viewModel.updateField(viewModel.state.copy(bire = date))
        },
        calendar.get(Calendar.YEAR) - 25,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Helper to get options from commonParams
    fun getCommonOptions(key: String): List<OptionItem> {
        val list = mutableListOf<OptionItem>()
        viewModel.commonParams?.getAsJsonArray(key)?.forEach { element ->
            val obj = element.asJsonObject
            list.add(OptionItem(obj.get("k").asString, obj.get("v").asString))
        }
        return list
    }

    if (showCamera) {
        KtpCameraView(
            onImageCaptured = { bitmap ->
                viewModel.uploadKtp(bitmap)
                showCamera = false
            },
            onClose = { showCamera = false }
        )
    } else {
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
                            if (!viewModel.canUpdateIdFile && viewModel.state.inm.isNotEmpty()) {
                                Toast.makeText(context, "Identitas ini tidak ada masalah, tidak perlu diupload ulang", Toast.LENGTH_SHORT).show()
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (viewModel.showOCRLoading) {
                                CircularProgressIndicator(color = Color(0xFFBD0100))
                            } else {
                                if (viewModel.capturedBitmap != null) {
                                    Image(
                                        bitmap = viewModel.capturedBitmap!!.asImageBitmap(),
                                        contentDescription = "Captured KTP",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.FillBounds
                                    )
                                } else if (viewModel.state.ktpUrl.isNotEmpty()) {
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
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = viewModel.state.funName,
                        onValueChange = { viewModel.updateField(viewModel.state.copy(funName = it)) },
                        label = { Text("Nama Lengkap (Sesuai KTP)") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // 3. Selection Fields
                    SelectableField(
                        label = "Jenis Kelamin",
                        value = viewModel.state.genn,
                        onClick = { 
                            actionSheetTitle = "Pilih Jenis Kelamin"
                            currentSelectionType = "gen"
                            showActionSheet = true
                        }
                    )

                    SelectableField(
                        label = "Tanggal Lahir",
                        value = viewModel.state.bire,
                        onClick = { datePickerDialog.show() }
                    )

                    SelectableField(
                        label = "Alamat Domisili",
                        value = viewModel.state.lvstr,
                        onClick = { 
                            addressMode = "l"
                            actionSheetTitle = "Pilih RT"
                            currentSelectionType = "rt"
                            showActionSheet = true 
                        }
                    )

                    SelectableField(
                        label = "Alamat Kantor",
                        value = viewModel.state.cstr,
                        onClick = { 
                            addressMode = "c"
                            actionSheetTitle = "Pilih RT"
                            currentSelectionType = "rt"
                            showActionSheet = true 
                        }
                    )

                    OutlinedTextField(
                        value = viewModel.state.eil,
                        onValueChange = { viewModel.updateField(viewModel.state.copy(eil = it)) },
                        label = { Text("Email (Gmail)") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Agreement Checkbox
                    if (viewModel.cmeData?.wiue == true) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.clickable { viewModel.checkAgree = !viewModel.checkAgree }) {
                            Checkbox(
                                checked = viewModel.checkAgree,
                                onCheckedChange = { viewModel.checkAgree = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFBD0100))
                            )
                            val annotatedString = buildAnnotatedString {
                                append("Saya telah membaca, memahami, dan menyetujui ")
                                withStyle(style = SpanStyle(color = Color(0xFFBD0100), fontWeight = FontWeight.Bold)) {
                                    append("Syarat dan Ketentuan")
                                }
                                append(" untuk verifikasi identitas.")
                            }
                            Text(text = annotatedString, fontSize = 12.sp, color = Color(0xFF262626), modifier = Modifier.padding(top = 12.dp))
                        }
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
                                viewModel.submitInfo(onSuccess = { onNextClick() })
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
    }

    // Modal Bottom Sheet for all selections
    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = actionSheetTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val optionsToShow = when (currentSelectionType) {
                    "gen" -> getCommonOptions("gen")
                    "rt" -> getCommonOptions("rt")
                    "rw" -> getCommonOptions("rw")
                    else -> viewModel.addressList.map { OptionItem(it.code ?: "", it.name ?: "") }
                }
                
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(optionsToShow) { item ->
                        ListItem(
                            headlineContent = { Text(item.value) },
                            modifier = Modifier.clickable {
                                handleAddressSelectionFlow(
                                    viewModel = viewModel,
                                    type = currentSelectionType,
                                    item = item,
                                    addressMode = addressMode,
                                    onNextStep = { nextType, nextTitle ->
                                        currentSelectionType = nextType
                                        actionSheetTitle = nextTitle
                                    },
                                    onClose = { showActionSheet = false }
                                )
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }

    // Confirmation Popup
    if (viewModel.showConfirmInfoPop) {
        Dialog(onDismissRequest = { viewModel.showConfirmInfoPop = false }) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KONFIRMASI DATA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Harap pastikan data sesuai dengan KTP, lakukan edit jika diperlukan:", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nama: ${viewModel.state.funName}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("NIK: ${viewModel.state.inm}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Jika terdapat ketidaksesuaian data dengan KTP maka akan mempengaruhi lamanya proses pengajuan pinjaman", fontSize = 12.sp, color = Color.Red)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { viewModel.showConfirmInfoPop = false }, modifier = Modifier.weight(1f)) { Text("Belum Sesuai") }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { 
                            viewModel.showConfirmInfoPop = false
                            viewModel.idNumConfirmed = true
                            viewModel.submitInfo { onNextClick() }
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))) { Text("Sesuai") }
                    }
                }
            }
        }
    }

    // Have Bill Popup
    if (viewModel.showHaveBillPop) {
        Dialog(onDismissRequest = { viewModel.showHaveBillPop = false }) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.iv_logo_ivoji_splash), contentDescription = null, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Anda pernah pinjaman di IVOJI", fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { 
                        viewModel.showHaveBillPop = false
                        // onNavigate("MyBill") 
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))) { Text("Untuk pembayaran") }
                }
            }
        }
    }

    // Error Popup (Retry logic)
    if (viewModel.showInfoErrorPop) {
        Dialog(onDismissRequest = { viewModel.showInfoErrorPop = false }) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.errMsg, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { viewModel.showInfoErrorPop = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))) { Text("Tidak, mohon direvisi") }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { 
                            viewModel.showInfoErrorPop = false
                            viewModel.submitInfo { onNextClick() }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Ya") }
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

/**
 * Logika Cascading Address: RT -> RW -> Province -> City -> District -> Village
 */
private fun handleAddressSelectionFlow(
    viewModel: BaseInfoViewModel,
    type: String,
    item: OptionItem,
    addressMode: String,
    onNextStep: (String, String) -> Unit,
    onClose: () -> Unit
) {
    val currentState = viewModel.state
    when (type) {
        "gen" -> {
            viewModel.updateField(currentState.copy(gen = item.key.toInt(), genn = item.value))
            onClose()
        }
        "rt" -> {
            if (addressMode == "l") {
                viewModel.updateField(currentState.copy(rtid = item.key, rtidn = item.value))
            } else {
                viewModel.updateField(currentState.copy(crtid = item.key, crtidn = item.value))
            }
            onNextStep("rw", "Pilih RW")
        }
        "rw" -> {
            if (addressMode == "l") {
                viewModel.updateField(currentState.copy(rwid = item.key, rwidn = item.value))
            } else {
                viewModel.updateField(currentState.copy(crwid = item.key, crwidn = item.value))
            }
            viewModel.loadAddresses(1, "0") // Start cascading address from Province
            onNextStep("province", "Pilih Provinsi")
        }
        "province" -> {
            if (addressMode == "l") {
                viewModel.updateField(currentState.copy(lpid = item.key, lpidn = item.value))
            } else {
                viewModel.updateField(currentState.copy(cpid = item.key, cpidn = item.value))
            }
            viewModel.loadAddresses(2, item.key)
            onNextStep("city", "Pilih Kota")
        }
        "city" -> {
            if (addressMode == "l") {
                viewModel.updateField(currentState.copy(lcid = item.key, lcidn = item.value))
            } else {
                viewModel.updateField(currentState.copy(ccid = item.key, ccidn = item.value))
            }
            viewModel.loadAddresses(3, item.key)
            onNextStep("district", "Pilih Kecamatan")
        }
        "district" -> {
            if (addressMode == "l") {
                viewModel.updateField(currentState.copy(ldid = item.key, ldidn = item.value))
            } else {
                viewModel.updateField(currentState.copy(cdid = item.key, cdidn = item.value))
            }
            viewModel.loadAddresses(4, item.key)
            onNextStep("viname", "Pilih Kelurahan/Desa")
        }
        "viname" -> {
            if (addressMode == "l") {
                val updatedState = currentState.copy(viid = item.key, viidn = item.value)
                val rtRw = "${updatedState.rtidn}/${updatedState.rwidn}"
                val finalAddr = "$rtRw ${updatedState.lpidn} ${updatedState.lcidn} ${updatedState.ldidn} ${updatedState.viidn}".trim()
                viewModel.updateField(updatedState.copy(lvstr = finalAddr))
            } else {
                val updatedState = currentState.copy(cviid = item.key, cviidn = item.value)
                val rtRw = "${updatedState.crtidn}/${updatedState.crwidn}"
                val finalAddr = "$rtRw ${updatedState.cpidn} ${updatedState.ccidn} ${updatedState.cdidn} ${updatedState.cviidn}".trim()
                viewModel.updateField(updatedState.copy(cstr = finalAddr))
            }
            onClose()
        }
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
