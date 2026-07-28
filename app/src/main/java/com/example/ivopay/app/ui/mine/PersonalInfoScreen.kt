package com.example.ivopay.app.ui.mine

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// Data Model State untuk PersonalInfo
data class PersonalInfoState(
    var motherName: String = "",
    var religion: String = "",
    var education: String = "",
    var housingType: String = "",
    var lengthOfStay: String = "",
    var loanPurpose: String = "",
    var residenceAddress: String = "",
    var detailAddress: String = "",

    // Bank Info
    var bankName: String = "",
    var bankAccountNumber: String = "",
    var bankAccountOwner: String = "",

    // Family Info
    var maritalStatus: String = "", // e.g., "Single", "Married" (Code 2)
    var familySize: String = "",
    var spouseName: String = "",
    var spouseBirthDate: String = "",
    var propertyAgreement: String = ""
)

// Data Model Opsi Dropdown
data class OptionItem(val key: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onBackClick: () -> Unit,
    onNextClick: (PersonalInfoState) -> Unit,
    religionOptions: List<OptionItem> = listOf(OptionItem("1", "Islam"), OptionItem("2", "Kristen"), OptionItem("3", "Katolik"), OptionItem("4", "Hindu"), OptionItem("5", "Buddha")),
    educationOptions: List<OptionItem> = listOf(OptionItem("1", "SMA/SMK"), OptionItem("2", "D3"), OptionItem("3", "S1"), OptionItem("4", "S2/S3")),
    housingTypeOptions: List<OptionItem> = listOf(OptionItem("1", "Milik Sendiri"), OptionItem("2", "Sewa/Kontrak"), OptionItem("3", "Milik Orang Tua")),
    lengthOfStayOptions: List<OptionItem> = listOf(OptionItem("1", "< 1 Tahun"), OptionItem("2", "1 - 3 Tahun"), OptionItem("3", "> 3 Tahun")),
    loanPurposeOptions: List<OptionItem> = listOf(OptionItem("1", "Modal Usaha"), OptionItem("2", "Kebutuhan Sehari-hari"), OptionItem("3", "Pendidikan")),
    bankOptions: List<OptionItem> = listOf(OptionItem("BCA", "Bank BCA"), OptionItem("MANDIRI", "Bank Mandiri"), OptionItem("BRI", "Bank BRI"), OptionItem("BNI", "Bank BNI")),
    maritalStatusOptions: List<OptionItem> = listOf(OptionItem("1", "Lajang"), OptionItem("2", "Menikah"), OptionItem("3", "Cerai")),
    familySizeOptions: List<OptionItem> = listOf(OptionItem("1", "1 Orang"), OptionItem("2", "2 Orang"), OptionItem("3", ">= 3 Orang")),
    propertyAgreementOptions: List<OptionItem> = listOf(OptionItem("1", "Ya"), OptionItem("0", "Tidak"))
) {
    val context = LocalContext.current
    var formState by remember { mutableStateOf(PersonalInfoState()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // BottomSheet Select State
    var activePickerTitle by remember { mutableStateOf("") }
    var activeOptionsList by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    var onOptionSelectedCallback by remember { mutableStateOf<((OptionItem) -> Unit)?>(null) }
    var showOptionBottomSheet by remember { mutableStateOf(false) }

    // DatePicker Pasangan
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            formState = formState.copy(spouseBirthDate = "$dayOfMonth/${month + 1}/$year")
        },
        calendar.get(Calendar.YEAR) - 25,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Helper untuk Membuka Selection Sheet
    fun openPicker(title: String, options: List<OptionItem>, onSelect: (OptionItem) -> Unit) {
        activePickerTitle = title
        activeOptionsList = options
        onOptionSelectedCallback = onSelect
        showOptionBottomSheet = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Pribadi", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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

            // --- 1. DATA PRIBADI ---
            // Nama Ibu Kandung
            OutlinedTextField(
                value = formState.motherName,
                onValueChange = { input ->
                    formState = formState.copy(motherName = input.filter { it.isLetter() || it.isWhitespace() })
                },
                label = { Text("Nama Ibu Kandung") },
                supportingText = { Text("*Nama lengkap ibu kandung (sesuai dengan KTP)", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Agama
            SelectableField(
                label = "Agama",
                value = formState.religion,
                onClick = {
                    openPicker("Agama", religionOptions) { formState = formState.copy(religion = it.value) }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pendidikan
            SelectableField(
                label = "Pendidikan",
                value = formState.education,
                onClick = {
                    openPicker("Pendidikan", educationOptions) { formState = formState.copy(education = it.value) }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tipe Tempat Tinggal
            SelectableField(
                label = "Tipe Tempat Tinggal",
                value = formState.housingType,
                onClick = {
                    openPicker("Tipe Tempat Tinggal", housingTypeOptions) { formState = formState.copy(housingType = it.value) }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lama Tinggal
            SelectableField(
                label = "Lama Tinggal",
                value = formState.lengthOfStay,
                onClick = {
                    openPicker("Lama Tinggal", lengthOfStayOptions) { formState = formState.copy(lengthOfStay = it.value) }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tujuan Pinjaman
            SelectableField(
                label = "Tujuan Pinjaman",
                value = formState.loanPurpose,
                onClick = {
                    openPicker("Tujuan Pinjaman", loanPurposeOptions) { formState = formState.copy(loanPurpose = it.value) }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alamat Tempat Tinggal / Domisili (Readonly)
            OutlinedTextField(
                value = formState.residenceAddress,
                onValueChange = {},
                label = { Text("Alamat tempat tinggal / domisili") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Detail Alamat
            OutlinedTextField(
                value = formState.detailAddress,
                onValueChange = { formState = formState.copy(detailAddress = it) },
                label = { Text("Alamat Detail (Gedung, No. Rumah, RT/RW)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. INFORMASI REKENING BANK ---
            Text("Rekening Kartu Bank", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF262626))
            Spacer(modifier = Modifier.height(12.dp))

            // Nama Bank
            SelectableField(
                label = "Nama Bank",
                value = formState.bankName,
                onClick = {
                    openPicker("Nama Bank", bankOptions) { formState = formState.copy(bankName = it.value) }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nomor Rekening
            OutlinedTextField(
                value = formState.bankAccountNumber,
                onValueChange = { input ->
                    formState = formState.copy(bankAccountNumber = input.filter { it.isDigit() })
                },
                label = { Text("Nomor Rekening") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nama Pemilik Rekening
            OutlinedTextField(
                value = formState.bankAccountOwner,
                onValueChange = { input ->
                    formState = formState.copy(bankAccountOwner = input.filter { it.isLetter() || it.isWhitespace() })
                },
                label = { Text("Nama Pemilik Rekening") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. INFORMASI KELUARGA ---
            Text("Informasi Keluarga", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF262626))
            Spacer(modifier = Modifier.height(12.dp))

            // Status Pernikahan
            SelectableField(
                label = "Status Pernikahan",
                value = formState.maritalStatus,
                onClick = {
                    openPicker("Status Pernikahan", maritalStatusOptions) { selected ->
                        formState = formState.copy(maritalStatus = selected.value)
                    }
                }
            )

            // Dinamis Form Pasangan Jika Status "Menikah"
            if (formState.maritalStatus == "Menikah") {
                Spacer(modifier = Modifier.height(12.dp))

                // Jumlah Tanggungan
                SelectableField(
                    label = "Jumlah Tanggungan / Anggota Keluarga",
                    value = formState.familySize,
                    onClick = {
                        openPicker("Jumlah Tanggungan", familySizeOptions) { formState = formState.copy(familySize = it.value) }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nama Lengkap Pasangan
                OutlinedTextField(
                    value = formState.spouseName,
                    onValueChange = { input ->
                        formState = formState.copy(spouseName = input.filter { it.isLetter() || it.isWhitespace() })
                    },
                    label = { Text("Nama Lengkap Pasangan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tanggal Lahir Pasangan
                SelectableField(
                    label = "Tanggal Lahir Pasangan",
                    value = formState.spouseBirthDate,
                    onClick = { datePickerDialog.show() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Perjanjian Pisah Harta
                SelectableField(
                    label = "Perjanjian Pisah Harta",
                    value = formState.propertyAgreement,
                    onClick = {
                        openPicker("Perjanjian Pisah Harta", propertyAgreementOptions) { formState = formState.copy(propertyAgreement = it.value) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Toast Text
            errorMessage?.let { msg ->
                Text(msg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Action Buttons
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
                        if (formState.motherName.isEmpty() || formState.bankAccountNumber.isEmpty()) {
                            errorMessage = "Harap lengkapi bidang yang wajib diisi!"
                        } else {
                            errorMessage = null
                            onNextClick(formState)
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

    // Modal Bottom Sheet untuk Dropdown Pilihan
    if (showOptionBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = activePickerTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                HorizontalDivider()
                activeOptionsList.forEach { option ->
                    Text(
                        text = option.value,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelectedCallback?.invoke(option)
                                showOptionBottomSheet = false
                            }
                            .padding(vertical = 14.dp)
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}

// Sub-komponen Input Dropdown
@Composable
fun SelectableField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}