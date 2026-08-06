package com.example.ivopay.app.ui.mine

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ivopay.R

// Data model untuk item foto/dokumen
data class LenderPhotoItem(
    val title: String,
    val desc: String,
    val imgName: String,
    var bitmap: Bitmap? = null,
    var url: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderBasicInfoScreen(
    viewModel: LenderBasicInfoViewModel,
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
    onSelectPhoto: (index: Int, onCaptured: (Bitmap) -> Unit) -> Unit = { _, _ -> }
) {
    // State Formulir
    var bankName by remember { mutableStateOf("") }
    var bankCode by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf(1) } // 1 = KTP, 2 = Passport
    var ktpNumber by remember { mutableStateOf("") }
    var passportNumber by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var npwpNumber by remember { mutableStateOf("") }
    
    // Alamat & Pekerjaan States (Selected Values)
    var rtKey by remember { mutableStateOf("") }
    var rtValue by remember { mutableStateOf("") }
    var rwKey by remember { mutableStateOf("") }
    var rwValue by remember { mutableStateOf("") }
    
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    
    var jobKey by remember { mutableStateOf("") }
    var jobValue by remember { mutableStateOf("") }
    var incomeKey by remember { mutableStateOf("") }
    var incomeValue by remember { mutableStateOf("") }
    
    var companyName by remember { mutableStateOf("") }
    var companyAddress by remember { mutableStateOf("") }

    // Dialog & Picker States
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showBankPicker by remember { mutableStateOf(false) }
    var showCommonPicker by remember { mutableStateOf(false) }
    
    // Picker Configuration
    var pickerTitle by remember { mutableStateOf("") }
    var pickerList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var onPickerSelected: ((String, String) -> Unit) by remember { mutableStateOf({ _, _ -> }) }
    
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    // List Dokumen
    val photoList = remember {
        mutableStateListOf(
            LenderPhotoItem("Foto tanda pengenal", "KTP or passport", "idfie"),
            LenderPhotoItem("Foto Selfie", "Upload foto selfie", "idhie"),
            LenderPhotoItem("Foto NPWP", "Foto NPWP", "npim"),
            LenderPhotoItem("Foto NIB", "Nomor Induk Berusaha", "pbli"),
            LenderPhotoItem("Bukti bank", "Mutasi rekening / koran", "pbst"),
            LenderPhotoItem("Tanda Tangan", "Belum Tanda tangan", "tgim")
        )
    }

    // Pre-fill data when userInfo is fetched
    LaunchedEffect(viewModel.userInfo) {
        viewModel.userInfo?.let { data ->
            // Bank Info
            data.bankAccount?.let { bac ->
                if (bankName.isEmpty()) bankName = bac.bankName ?: ""
                if (accountNumber.isEmpty()) accountNumber = bac.accountNumber ?: ""
                if (accountName.isEmpty()) accountName = bac.accountOwner ?: ""
            }

            // Personal Info
            data.personalInfo?.let { pi ->
                if (fullName.isEmpty()) fullName = pi.fullName ?: ""
                if (docType == 1) { 
                    if (ktpNumber.isEmpty()) ktpNumber = pi.idNumber ?: ""
                } else {
                    if (passportNumber.isEmpty()) passportNumber = pi.idNumber ?: ""
                }
                if (birthPlace.isEmpty()) birthPlace = pi.location?.birthPlace ?: ""
                if (birthDate.isEmpty()) birthDate = pi.birthDate ?: ""
                if (email.isEmpty()) email = pi.email ?: ""
                if (npwpNumber.isEmpty()) npwpNumber = pi.npwpNumber ?: ""

                // Location
                pi.location?.let { loc ->
                    if (province.isEmpty()) province = loc.provinceName ?: ""
                    if (city.isEmpty()) city = loc.cityName ?: ""
                    if (postalCode.isEmpty()) postalCode = loc.postalCode ?: ""
                    if (addressDetail.isEmpty()) addressDetail = loc.addressDetail ?: ""
                    
                    if (rtValue.isEmpty()) rtValue = loc.rtName ?: ""
                    if (rtKey.isEmpty()) rtKey = loc.rtKey?.toString() ?: ""
                    if (rwValue.isEmpty()) rwValue = loc.rwName ?: ""
                    if (rwKey.isEmpty()) rwKey = loc.rwKey?.toString() ?: ""
                }
            }

            // Work Info
            data.workInfo?.let { wi ->
                if (jobValue.isEmpty()) jobValue = wi.jobName ?: ""
                if (jobKey.isEmpty()) jobKey = wi.jobKey?.toString() ?: ""
                if (incomeValue.isEmpty()) incomeValue = wi.incomeName ?: ""
                if (incomeKey.isEmpty()) incomeKey = wi.incomeKey?.toString() ?: ""
                if (companyName.isEmpty()) companyName = wi.companyName ?: ""
                if (companyAddress.isEmpty()) companyAddress = wi.workLocation?.companyAddress ?: ""
            }

            // Images URLs
            data.images?.let { ide ->
                if (photoList[0].url == null) photoList[0] = photoList[0].copy(url = ide.idCardUrl)
                if (photoList[1].url == null) photoList[1] = photoList[1].copy(url = ide.selfieUrl)
                if (photoList[2].url == null) photoList[2] = photoList[2].copy(url = ide.npwpUrl)
                if (photoList[3].url == null) photoList[3] = photoList[3].copy(url = ide.nibUrl)
                if (photoList[4].url == null) photoList[4] = photoList[4].copy(url = ide.bankStatementUrl)
                if (photoList[5].url == null) photoList[5] = photoList[5].copy(url = ide.signatureUrl)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Pribadi", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F8F8))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section: Bank Card Account
            SectionTitle("Bank Card Account")

            ClickableField(
                label = "Nama Bank",
                value = bankName,
                onClick = { showBankPicker = true }
            )
            
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it.filter { char -> char.isDigit() } },
                label = { Text("Account Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Owner Name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Section: Document Photo
            SectionTitle("Document photo")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                itemsIndexed(photoList) { index, item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(4.dp))
                            .clickable {
                                if (index == 5) {
                                    showSignatureDialog = true
                                } else {
                                    onSelectPhoto(index) { bitmap ->
                                        photoList[index] = photoList[index].copy(bitmap = bitmap)
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.bitmap != null) {
                                Image(
                                    bitmap = item.bitmap!!.asImageBitmap(),
                                    contentDescription = item.title,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (!item.url.isNullOrEmpty()) {
                                AsyncImage(
                                    model = item.url,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(
                                        id = if (index == 5) R.drawable.iv_data_ic_sign else R.drawable.iv_data_ic_upload_lender
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                Text(item.desc, fontSize = 10.sp, color = Color(0x8C000000), maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Section: Other Information
            SectionTitle("Other information")

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Document Type Selector
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                FilterChip(
                    selected = docType == 1,
                    onClick = { docType = 1 },
                    label = { Text("KTP") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = docType == 2,
                    onClick = { docType = 2 },
                    label = { Text("Passport") }
                )
            }

            if (docType == 1) {
                OutlinedTextField(
                    value = ktpNumber,
                    onValueChange = { if (it.length <= 16) ktpNumber = it.filter { c -> c.isDigit() } },
                    label = { Text("KTP Number (16 Digit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            } else {
                OutlinedTextField(
                    value = passportNumber,
                    onValueChange = { if (it.length <= 9) passportNumber = it },
                    label = { Text("Passport (9 Karakter)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            OutlinedTextField(
                value = birthPlace,
                onValueChange = { birthPlace = it },
                label = { Text("Place of Birth") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Date of birth (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Gmail mailbox") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = npwpNumber,
                onValueChange = { if (it.length <= 15) npwpNumber = it.filter { c -> c.isDigit() } },
                label = { Text("NPWP number (15 Digit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Split RT & RW into Pickers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ClickableField(
                        label = "RT",
                        value = rtValue,
                        onClick = {
                            pickerTitle = "Pilih RT"
                            pickerList = viewModel.getCommonList("rt")
                            onPickerSelected = { k, v -> rtKey = k; rtValue = v }
                            showCommonPicker = true
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ClickableField(
                        label = "RW",
                        value = rwValue,
                        onClick = {
                            pickerTitle = "Pilih RW"
                            pickerList = viewModel.getCommonList("rw")
                            onPickerSelected = { k, v -> rwKey = k; rwValue = v }
                            showCommonPicker = true
                        }
                    )
                }
            }

            OutlinedTextField(
                value = province,
                onValueChange = { province = it },
                label = { Text("Province") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                label = { Text("Zip Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = addressDetail,
                onValueChange = { addressDetail = it },
                label = { Text("Detailed Address") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Job & Income Pickers
            ClickableField(
                label = "Job Position",
                value = jobValue,
                onClick = {
                    pickerTitle = "Pilih Pekerjaan"
                    pickerList = viewModel.getCommonList("ljos")
                    onPickerSelected = { k, v -> jobKey = k; jobValue = v }
                    showCommonPicker = true
                }
            )

            ClickableField(
                label = "Annual income",
                value = incomeValue,
                onClick = {
                    pickerTitle = "Pilih Penghasilan"
                    pickerList = viewModel.getCommonList("linrs")
                    onPickerSelected = { k, v -> incomeKey = k; incomeValue = v }
                    showCommonPicker = true
                }
            )

            // Section: Company Information
            SectionTitle("Company information")

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = companyAddress,
                onValueChange = { companyAddress = it },
                label = { Text("Company registered address") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = onSubmitSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Selanjutnya", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- MODALS & DIALOGS ---

    // 1. Bank Picker
    if (showBankPicker) {
        ModalBottomSheet(
            onDismissRequest = { showBankPicker = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)) {
                Text("Pilih Bank", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari bank...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                val filteredBanks = viewModel.bankList.filter {
                    it.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                            it.name?.contains(searchQuery, ignoreCase = true) == true
                }
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredBanks) { bank ->
                        ListItem(
                            headlineContent = { Text(bank.fullName ?: bank.name ?: "") },
                            modifier = Modifier.clickable {
                                bankName = bank.fullName ?: bank.name ?: ""
                                bankCode = bank.name ?: ""
                                showBankPicker = false
                                searchQuery = ""
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }

    // 2. Common Picker (RT, RW, Job, Income)
    if (showCommonPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCommonPicker = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f).padding(16.dp)) {
                Text(pickerTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(pickerList) { item ->
                        ListItem(
                            headlineContent = { Text(item.second) },
                            modifier = Modifier.clickable {
                                onPickerSelected(item.first, item.second)
                                showCommonPicker = false
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }

    // 3. Signature Pad Dialog
    if (showSignatureDialog) {
        SignaturePadDialog(
            onDismiss = { showSignatureDialog = false },
            onConfirm = { signatureBitmap ->
                photoList[5] = photoList[5].copy(bitmap = signatureBitmap)
                showSignatureDialog = false
            }
        )
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFE5455))
        }
    }
}

@Composable
fun ClickableField(label: String, value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        enabled = false,
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(24.dp)) },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color(0xFFCCCCCC),
            disabledLabelColor = Color.Gray,
            disabledTrailingIconColor = Color.Gray
        )
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF262626),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SignaturePadDialog(onDismiss: () -> Unit, onConfirm: (Bitmap) -> Unit) {
    val path = remember { Path() }
    var drawTrigger by remember { mutableStateOf(0) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tanda Tangan Digital", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Tutup") }
                }
                Text(text = "Harap tanda tangan di bawah ini, lalu klik OK.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)).pointerInput(Unit) {
                    detectDragGestures(onDragStart = { offset -> path.moveTo(offset.x, offset.y) }, onDrag = { change, _ -> path.lineTo(change.position.x, change.position.y); drawTrigger++ })
                }) {
                    Canvas(modifier = Modifier.fillMaxSize()) { drawTrigger.let { drawPath(path = path, color = Color.Black, style = Stroke(width = 4.dp.toPx())) } }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { path.reset(); drawTrigger++ }) { Text("Clear", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { val bitmap = Bitmap.createBitmap(500, 300, Bitmap.Config.ARGB_8888); onConfirm(bitmap) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) { Text("OK", color = Color.White) }
                }
            }
        }
    }
}
