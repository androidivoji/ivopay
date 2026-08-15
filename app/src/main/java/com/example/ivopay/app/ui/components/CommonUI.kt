package com.example.ivopay.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ivopay.R

// Model data untuk item pilihan (dropdown/sheet)
data class OptionItem(
    val key: String,
    val value: String,
    val pc: String? = null // Kode Pos (Postal Code)
)

@Composable
fun SelectableField(
    label: String, 
    value: String, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().padding(top = 12.dp)) {
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
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTextColor = MaterialTheme.colorScheme.onSurface
            ),
            trailingIcon = { 
                Icon(
                    painter = painterResource(id = R.drawable.iv_set_right_arrow), 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp)
                ) 
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}
