package com.example.ivopay.app.ui.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Model State Informasi Pekerjaan
data class JobInfoData(
    var con: String = "",       // Nama Perusahaan
    var syamt: String = "",     // Pendapatan Bulanan
    var joiun: String = "",     // Jenis Usaha (Business Type Label)
    var join: String = "",      // Jenis Pekerjaan (Work Type Label)
    var joi: String = "",       // Jenis Pekerjaan Key
    var iniun: String = "",     // Jenis Industri Label
    var inin: String = "",      // Sektor Ekonomi Label
    var ini: String = "",       // Sektor Ekonomi Key
    var jorkn: String = "",     // Jabatan Label
    var jork: String = "",      // Jabatan Key
    var wkdnn: String = "",     // Lama Bekerja Label
    var wkdn: String = "",      // Lama Bekerja Key
    var cstr: String = "",      // Alamat Kantor Gabungan
    var cdel: String = "",      // Detail Alamat Perusahaan
    var wkptie: String = "",    // Image/URL Bukti Kerja
    var wkptie_yep: String = "" // Tipe Dokumen Bukti Kerja
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobInfoV2Screen(
    onBackClick: () -> Unit,
    onSubmitClick: (JobInfoData) -> Unit,
    initialData: JobInfoData = JobInfoData(),
    isWorkProofMandatory: Boolean = false,
    onTakeWorkProofPhoto: (typeIndex: Int, callback: (String) -> Unit) -> Unit = { _, _ -> }
) {
    var jobInfo by remember { mutableStateOf(initialData) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog & Bottom Sheet States
    var showWorkProofDialog by remember { mutableStateOf(false) }
    var showPickerBottomSheet by remember { mutableStateOf(false) }
    var pickerTitle by remember { mutableStateOf("") }
    var currentPickerOptions by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    var onOptionSelectedAction by remember { mutableStateOf<((OptionItem) -> Unit)?>(null) }

    val workDocumentList = listOf(
        "1. ID card karyawan",
        "2. Slip gaji ( 1 bulan terakhir )",
        "3. Mutasi gaji ( 1 bulan terakhir )",
        "4. Surat keterangan kerja",
        "5. Foto selfie beserta lingkungan kerja"
    )

    // Validasi Pendapatan Bulanan
    fun checkMonthlyIncome(valStr: String): String? {
        if (valStr.isEmpty()) return "Konten teks tidak boleh kosong"
        val cleaned = valStr.replace("[.,]".toRegex(), "")
        val amount = cleaned.toLongOrNull() ?: 0L
        if (amount < 3_000_000L || amount > 80_000_000L) {
            return "Kisaran pengisian pendapatan bulanan 3.000.000-80.000.000"
        }
        return null
    }

    // Validasi Keseluruhan Form
    fun validateForm(): Boolean {
        if (jobInfo.con.trim().isEmpty()) {
            errorMessage = "Nama Perusahaan tidak boleh kosong"
            return false
        }
        val incomeErr = checkMonthlyIncome(jobInfo.syamt)
        if (incomeErr != null) {
            errorMessage = incomeErr
            return false
        }
        if (jobInfo.joiun.isEmpty()) {
            errorMessage = "Harap pilih Jenis Usaha"
            return false
        }
        if (jobInfo.cdel.trim().isEmpty()) {
            errorMessage = "Alamat lengkap perusahaan tidak boleh kosong"
            return false
        }
        if (isWorkProofMandatory && jobInfo.wkptie.isEmpty()) {
            errorMessage = "Harap pilih foto bukti kerja terlebih dahulu"
            return false
        }

        errorMessage = null
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informasi Pekerjaan", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Nama Perusahaan
            OutlinedTextField(
                value = jobInfo.con,
                onValueChange = { jobInfo = jobInfo.copy(con = it) },
                label = { Text("Nama Perusahaan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Pendapatan Bulanan
            val incomeErr = if (jobInfo.syamt.isNotEmpty()) checkMonthlyIncome(jobInfo.syamt) else null
            OutlinedTextField(
                value = jobInfo.syamt,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    jobInfo = jobInfo.copy(syamt = filtered)
                },
                label = { Text("Pendapatan Bulanan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = incomeErr != null,
                supportingText = {
                    if (incomeErr != null) {
                        Text(incomeErr, color = Color.Red, fontSize = 12.sp)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Jenis Usaha
            SelectableField(
                label = "Jenis Usaha",
                value = jobInfo.joiun,
                onClick = {
                    pickerTitle = "Jenis Usaha"
                    currentPickerOptions = listOf(
                        OptionItem("1", "Manufaktur / Industri"),
                        OptionItem("2", "Perdagangan / Ritel"),
                        OptionItem("3", "Jasa / Teknologi"),
                        OptionItem("4", "Keuangan / Perbankan")
                    )
                    onOptionSelectedAction = { selected ->
                        jobInfo = jobInfo.copy(joiun = selected.value)
                    }
                    showPickerBottomSheet = true
                }
            )

            // Dynamic Job Fields (ditampilkan jika Jenis Usaha diisi)
            if (jobInfo.joiun.isNotEmpty() || jobInfo.join.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Jenis Pekerjaan
                SelectableField(
                    label = "Jenis Pekerjaan",
                    value = jobInfo.join,
                    onClick = {
                        pickerTitle = "Jenis Pekerjaan"
                        currentPickerOptions = listOf(
                            OptionItem("1", "Karyawan Tetap"),
                            OptionItem("2", "Karyawan Kontrak"),
                            OptionItem("3", "Pekerja Lepas / Freelance")
                        )
                        onOptionSelectedAction = { selected ->
                            jobInfo = jobInfo.copy(join = selected.value, joi = selected.key)
                        }
                        showPickerBottomSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sektor Ekonomi
                SelectableField(
                    label = "Sektor Ekonomi",
                    value = jobInfo.inin,
                    onClick = {
                        pickerTitle = "Sektor Ekonomi"
                        currentPickerOptions = listOf(
                            OptionItem("1", "Swasta"),
                            OptionItem("2", "BUMN / BUMD"),
                            OptionItem("3", "Pemerintahan")
                        )
                        onOptionSelectedAction = { selected ->
                            jobInfo = jobInfo.copy(inin = selected.value, ini = selected.key)
                        }
                        showPickerBottomSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Jabatan
                SelectableField(
                    label = "Jabatan",
                    value = jobInfo.jorkn,
                    onClick = {
                        pickerTitle = "Jabatan"
                        currentPickerOptions = listOf(
                            OptionItem("1", "Staf / Anggota"),
                            OptionItem("2", "Supervisor / Manager"),
                            OptionItem("3", "Direktur / Eksekutif")
                        )
                        onOptionSelectedAction = { selected ->
                            jobInfo = jobInfo.copy(jorkn = selected.value, jork = selected.key)
                        }
                        showPickerBottomSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Lama Bekerja
                SelectableField(
                    label = "Lama Bekerja",
                    value = jobInfo.wkdnn,
                    onClick = {
                        pickerTitle = "Lama Bekerja"
                        currentPickerOptions = listOf(
                            OptionItem("1", "< 1 Tahun"),
                            OptionItem("2", "1 - 3 Tahun"),
                            OptionItem("3", "3 - 5 Tahun"),
                            OptionItem("4", "> 5 Tahun")
                        )
                        onOptionSelectedAction = { selected ->
                            jobInfo = jobInfo.copy(wkdnn = selected.value, wkdn = selected.key)
                        }
                        showPickerBottomSheet = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alamat Kantor (Readonly Summary)
            OutlinedTextField(
                value = jobInfo.cstr,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Alamat kantor") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color(0xFF262626),
                    disabledBorderColor = Color(0xFFCCCCCC),
                    disabledLabelColor = Color(0xFF666666)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Detail Alamat Perusahaan
            OutlinedTextField(
                value = jobInfo.cdel,
                onValueChange = { jobInfo = jobInfo.copy(cdel = it) },
                label = { Text("Alamat Lengkap Perusahaan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kartu Upload Bukti Kerja
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWorkProofDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isWorkProofMandatory) "Bukti kerja (Wajib)" else "Bukti kerja (Opsional)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF262626)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (jobInfo.wkptie.isNotEmpty()) {
                        Text(
                            text = "Foto Terpilih ✓",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = "+ Ambil Foto Bukti Kerja",
                            color = Color(0xFFBD0100),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Banner Info Cepat
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Unggah bukti kerja untuk mempercepat verifikasi.",
                    fontSize = 12.sp,
                    color = Color(0x66000000)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tampilan Pesan Error
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Button Navigasi (Sebelumnya & Selanjutnya)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Sebelumnya", color = Color(0xFF262626))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (validateForm()) {
                            onSubmitClick(jobInfo)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Selanjutnya", color = Color.White)
                }
            }
        }
    }

    // Modal Dialog Pemilihan Dokumen Bukti Kerja
    if (showWorkProofDialog) {
        AlertDialog(
            onDismissRequest = { showWorkProofDialog = false },
            title = {
                Text(
                    text = if (isWorkProofMandatory) "Bukti kerja" else "Bukti kerja (Tidak Wajib)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isWorkProofMandatory)
                            "Diwajibkan mengupload salah satu dokumen:"
                        else
                            "*Mengunggah bukti kerja dapat mempercepat proses audit:",
                        fontSize = 13.sp,
                        color = if (isWorkProofMandatory) Color(0xFF262626) else Color(0xFFD93717),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    HorizontalDivider()
                    workDocumentList.forEachIndexed { index, doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showWorkProofDialog = false
                                    onTakeWorkProofPhoto(index) { photoPath ->
                                        jobInfo = jobInfo.copy(
                                            wkptie = photoPath,
                                            wkptie_yep = (index + 1).toString()
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = doc, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWorkProofDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White
        )
    }

    // Modal Bottom Sheet Generik untuk Seleksi
    if (showPickerBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPickerBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = pickerTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                HorizontalDivider()
                currentPickerOptions.forEach { option ->
                    Text(
                        text = option.value,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelectedAction?.invoke(option)
                                showPickerBottomSheet = false
                            }
                            .padding(vertical = 14.dp)
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}