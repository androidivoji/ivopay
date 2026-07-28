package com.example.ivopay.app.ui.mine

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Model Kontak Darurat
data class EmergencyContact(
    var funkontak: String = "",   // Nama Lengkap Kontak
    var phe: String = "",   // Nomor Telepon (tanpa +62 / 0)
    var rel: String = "",   // Key/Code Hubungan
    var reln: String = ""   // Label Hubungan
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(
    onBackClick: () -> Unit,
    onNextClick: (List<EmergencyContact>) -> Unit,
    userLoginPhone: String = "", // Nomor ponsel login pengguna untuk validasi
    relationOptions: List<OptionItem> = listOf(
        OptionItem("1", "Orang Tua"),
        OptionItem("2", "Pasangan"),
        OptionItem("3", "Saudara"),
        OptionItem("4", "Teman"),
        OptionItem("5", "Rekan Kerja")
    )
) {
    // Inisialisasi daftar 2 kontak darurat
    var contacts by remember {
        mutableStateOf(
            listOf(
                EmergencyContact(),
                EmergencyContact()
            )
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // BottomSheet Picker State
    var selectedContactIndex by remember { mutableIntStateOf(0) }
    var showRelationBottomSheet by remember { mutableStateOf(false) }

    // Fungsi Validasi Nomor Telepon
    fun validatePhone(phone: String): String? {
        if (phone.isEmpty()) return "Nomor telepon tidak boleh kosong"
        if (!phone.startsWith("8")) return "Masukkan nomor telepon yang dimulai dengan 8"
        if (phone.length < 9 || phone.length > 12) return "Nomor telepon harus 9-12 digit"
        return null
    }

    // Fungsi Validasi Keseluruhan Kontak Sebelum Submit
    fun validateContacts(): Boolean {
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

            // Cek jika mengisi nomor hp sendiri (nomor login)
            val cleanLoginPhone = userLoginPhone.removePrefix("0").removePrefix("+62")
            if (cleanLoginPhone.isNotEmpty() && contact.phe == cleanLoginPhone) {
                errorMessage = "Dilarang mengisi nomor ponsel login sebagai kontak darurat"
                return false
            }
        }

        // Cek Duplikasi Nomor Kontak
        val phoneList = contacts.map { it.phe }
        if (phoneList.distinct().size != phoneList.size) {
            errorMessage = "Jangan menggunakan nomor kontak yang sama"
            return false
        }

        errorMessage = null
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informasi Kontak", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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

            // Loop Form Kontak Darurat
            contacts.forEachIndexed { index, contact ->
                Text(
                    text = "Kontak Darurat ${index + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF262626)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Input Nomor Telepon dengan Prefix (+62)
                val phoneError = validatePhone(contact.phe)
                OutlinedTextField(
                    value = contact.phe,
                    onValueChange = { input ->
                        // Filter hanya angka dan hilangkan 0 di depan jika diisi 08xx
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

                // Input Nama Lengkap
                OutlinedTextField(
                    value = contact.funkontak,
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

                // Select Hubungan / Relasi
                SelectableField(
                    label = "Hubungan",
                    value = contact.reln,
                    onClick = {
                        selectedContactIndex = index
                        showRelationBottomSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tampilan Pesan Error Validation
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
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
                        if (validateContacts()) {
                            onNextClick(contacts)
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