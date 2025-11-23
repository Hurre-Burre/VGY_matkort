package com.example.vgy_matkort.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SetBalanceDialog(
    currentBalance: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    com.example.vgy_matkort.ui.components.KeypadDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        title = "Ange nuvarande saldo",
        initialValue = "", // Start empty to force user to enter new value, or could be currentBalance.toString()
        subtitle = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text(
                    text = "Nuvarande saldo: $currentBalance kr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.vgy_matkort.ui.theme.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vill du ta reda på ditt saldo? Ring 08 681 81 37",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.example.vgy_matkort.ui.theme.PrimaryBlue,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    )
}
