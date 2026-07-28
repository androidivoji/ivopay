package com.example.ivopay.app.ui.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Model untuk Data Alamat / Personal Info
data class UserAddressInfo(
    var lvstr: String = "",  // String Gabungan Alamat Domisili
    var del: String = "",    // Detail Alamat Tempat Tinggal
    var cstr: String = "",   // String Gabungan Alamat Kantor
    var cdel: String = ""    // Detail Alamat Perusahaan / Kantor
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoV2Screen(
    onBackClick: () -> Unit,
    onSubmitClick: (contacts: List<EmergencyContact>, addressInfo: UserAddressInfo) -> Unit,
    userLoginPhone: String = "",
    initialAddressInfo: UserAddressInfo = UserAddressInfo(),
    relationOptions: List<OptionItem> = listOf(
        OptionItem("1", "Orang Tua"),
        OptionItem("2", "Pasangan"),
        OptionItem("3", "Saudara"),
        OptionItem("4", "Teman"),
        OptionItem("5", "Rekan Kerja")
    )
) {
    // State Kontak Darurat (2 Kontak)
    var contacts by remember {
        mutableStateOf(
            listOf(
                EmergencyContact(),
                EmergencyContact()
            )
        )
    }

    // State Data Alamat
    var addressInfo by remember { mutableStateOf(initialAddressInfo) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // BottomSheet Picker State
    var selectedContactIndex by remember { mutableIntStateOf(0) }
    var showRelationBottomSheet by remember { mutableStateOf(false) }

    // Validation Helpers
    fun validatePhone(phone: String): String? {
        if (phone.isEmpty()) return "Nomor telepon tidak boleh kosong"
        if (!phone.startsWith("8")) return "Masukkan nomor telepon yang dimulai dengan 8"
        if (phone.length !in 9..12) return "Nomor telepon adalah 9-12 digit, harap verifikasi"
        return null
    }

    fun validateAll(): Boolean {
        // 1. Validasi Kontak Darurat
        for ((index, contact) in contacts.withIndex()) {
            if (contact.phe.isEmpty()) {
                errorMessage = "Nomor telepon Kontak Darurat ${index + 1} tidak boleh kosong"
                return false
            }
            val phoneErr = validatePhone(contact.phe)
            if (phoneErr != null) {
                errorMessage = "Kontak Darurat ${index + 1}: $phoneErr"
                return false
            }
            if (contact.funkontak.trim().isEmpty()) {
                errorMessage = "Nama Kontak Darurat ${index + 1} tidak boleh kosong"
                return false
            }
            if (contact.reln.isEmpty()) {
                errorMessage = "Hubungan Kontak Darurat ${index + 1} belum dipilih"
                return false
            }

            // Cek jika nomor kontak sama dengan nomor login
            val cleanLoginPhone = userLoginPhone.removePrefix("0").removePrefix("+62")
            if (cleanLoginPhone.isNotEmpty() && contact.phe == cleanLoginPhone) {
                errorMessage = "Dilarang mengisi nomor ponsel login sebagai kontak darurat"
                return false
            }
        }

        // Cek duplikasi nomor
        val phoneList = contacts.map { it.phe }
        if (phoneList.distinct().size != phoneList.size) {
            errorMessage = "Jangan ulangi nomor kontak"
            return false
        }

        // 2. Validasi Form Alamat
        if (addressInfo.del.trim().isEmpty()) {
            errorMessage = "Detail alamat tempat tinggal tidak boleh kosong"
            return false
        }
        if (addressInfo.cdel.trim().isEmpty()) {
            errorMessage = "Alamat lengkap perusahaan tidak boleh kosong"
            return false
        }

        errorMessage = null
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Informasi Kontak",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
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

            // --- SEKSI 1: KONTAK DARURAT ---
            contacts.forEachIndexed { index, contact ->
                Text(
                    text = "Kontak Darurat ${index + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF262626)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Input Phone
                val phoneError = validatePhone(contact.phe)
                OutlinedTextField(
                    value = contact.phe,
                    onValueChange = { input ->
                        var filtered = input.filter { it.isDigit() }
                        if (filtered.startsWith("0")) {
                            filtered = filtered.drop(1)
                        }
                        if (filtered.length <= 12) {
                            val updated = contacts.toMutableList()
                            updated[index] = updated[index].copy(phe = filtered)
                            contacts = updated
                        }
                    },
                    label = { Text("Nomor Telepon") },
                    prefix = {
                        Text(
                            "+62 ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF262626)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = contact.phe.isNotEmpty() && phoneError != null,
                    supportingText = {
                        if (contact.phe.isNotEmpty() && phoneError != null) {
                            Text(phoneError, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input Nama
                OutlinedTextField(
                    value = contact.funkontak ,
                            onValueChange = { input ->
                        val filtered = input.filter { it.isLetter() || it.isWhitespace() }
                        val updated = contacts.toMutableList()
                        updated[index] = updated[index].copy(funkontak = filtered)
                        contacts = updated
                    },
                    label = { Text("Nama") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select Hubungan
                SelectableField(
                    label = "Hubungan",
                    value = contact.reln,
                    onClick = {
                        selectedContactIndex = index
                        showRelationBottomSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- SEKSI 2: DATA PRIBADI (ALAMAT) ---
            Text(
                text = "Data Pribadi",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF262626)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Alamat Domisili (Readonly / Summary)
            OutlinedTextField(
                value = addressInfo.lvstr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Alamat tempat tinggal / domisili") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color(0xFF262626),
                    disabledBorderColor = Color(0xFFCCCCCC),
                    disabledLabelColor = Color(0xFF666666)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Detail Alamat Domisili
            OutlinedTextField(
                value = addressInfo.del,
                onValueChange = { addressInfo = addressInfo.copy(del = it) },
                label = { Text("Detail Alamat") },
                placeholder = { Text("Masukkan detail jalan, nomor rumah, dsb.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alamat Kantor (Readonly / Summary)
            OutlinedTextField(
                value = addressInfo.cstr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Alamat kantor") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color(0xFF262626),
                    disabledBorderColor = Color(0xFFCCCCCC),
                    disabledLabelColor = Color(0xFF666666)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Detail Alamat Kantor
            OutlinedTextField(
                value = addressInfo.cdel,
                onValueChange = { addressInfo = addressInfo.copy(cdel = it) },
                label = { Text("Alamat Lengkap Perusahaan") },
                placeholder = { Text("Masukkan gedung, lantai, jalan, dsb.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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

            // --- BUTTON SUBMIT ---
            Button(
                onClick = {
                    if (validateAll()) {
                        onSubmitClick(contacts, addressInfo)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Selanjutnya", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet untuk Memilih Hubungan Kontak
    if (showRelationBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRelationBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Hubungan Kontak Darurat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                HorizontalDivider()
                relationOptions.forEach { option ->
                    Text(
                        text = option.value,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = contacts.toMutableList()
                                updated[selectedContactIndex] = updated[selectedContactIndex].copy(
                                    rel = option.key,
                                    reln = option.value
                                )
                                contacts = updated
                                showRelationBottomSheet = false
                            }
                            .padding(vertical = 14.dp)
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}