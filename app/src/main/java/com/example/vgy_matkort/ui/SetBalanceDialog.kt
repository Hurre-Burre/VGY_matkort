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
    var balanceInput by remember { mutableStateOf(currentBalance.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ange nuvarande saldo") },
        text = {
            Column {
                Text(
                    text = "Nuvarande saldo: $currentBalance kr",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Ange det saldo du faktiskt har just nu. Appen kommer lägga till en korrigeringstransaktion för att justera saldot.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { 
                        balanceInput = it
                        isError = false
                    },
                    label = { Text("Nytt saldo (kr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Ange ett giltigt belopp",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newBalance = balanceInput.toIntOrNull()
                    if (newBalance != null) {
                        onConfirm(newBalance)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Bekräfta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
}
