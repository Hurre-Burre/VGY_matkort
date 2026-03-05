package com.example.vgy_matkort.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Preset
import com.example.vgy_matkort.data.Restaurant
import com.example.vgy_matkort.data.Transaction
import com.example.vgy_matkort.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    uiState: UiState,
    transactions: List<Transaction>,
    presets: List<Preset>,
    restaurants: List<Restaurant>,
    onAddTransaction: (Int, String) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onAddPreset: (Int, String) -> Unit,
    onDeletePreset: (Preset) -> Unit,
    onNavigateToWeeklySummary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isHapticEnabled: Boolean,
    currentTheme: AppTheme,
    language: String = "sv"
) {
    val haptic = LocalHapticFeedback.current
    var amountText by remember { mutableStateOf("") }
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }
    var pendingAmount by remember { mutableStateOf<Int?>(null) }
    val quickAmount = amountText.toIntOrNull() ?: 0
    val isQuickAmountValid = quickAmount in 1..90

    val darkUi = MaterialTheme.colorScheme.onSurface.luminance() > 0.5f
    val textPrimary = if (darkUi) Color.White else Color(0xFF101114)
    val textSecondary = if (darkUi) Color(0xCCFFFFFF) else Color(0xB3101114)
    val accent = when (currentTheme) {
        AppTheme.System -> if (!darkUi) Color(0xFF2FAE4A) else Color(0xFF68D77E)
        AppTheme.Signature -> Color(0xFF39C463)
        AppTheme.Blue -> Color(0xFF73B3FF)
        AppTheme.Green -> Color(0xFF7EEDA2)
        AppTheme.Red -> Color(0xFFFF8E8E)
        AppTheme.Orange -> Color(0xFFFF9A43)
        AppTheme.Purple -> Color(0xFFC9A3FF)
        AppTheme.Pink -> Color(0xFFFFA0C8)
    }
    val buttonBg = when (currentTheme) {
        AppTheme.System -> if (!darkUi) Color(0xFFE3B84D) else Color(0xFFCEAA4A)
        AppTheme.Signature -> Color(0xFF2486F4)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
    }
    val inputBg = if (darkUi) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.07f)
    val presetBg = if (darkUi) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.10f)
    val addBg = buttonBg.copy(alpha = if (darkUi) 0.78f else 0.68f)
    val isEnglish = language == "en"

    if (showAddPresetDialog) {
        com.example.vgy_matkort.ui.components.AddPresetDialog(
            onDismiss = { showAddPresetDialog = false },
            onConfirm = { amount, label ->
                onAddPreset(amount, label)
                showAddPresetDialog = false
            }
        )
    }

    if (pendingAmount != null) {
        com.example.vgy_matkort.ui.components.AddTransactionDialog(
            amount = pendingAmount!!,
            restaurants = restaurants,
            onDismiss = { pendingAmount = null },
            onConfirm = { amount, restaurantName ->
                onAddTransaction(amount, restaurantName)
                pendingAmount = null
            }
        )
    }

    if (presetToDelete != null) {
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Ta bort preset") },
            text = { Text("Vill du ta bort '${presetToDelete?.label}'?") },
            confirmButton = {
                TextButton(onClick = {
                    presetToDelete?.let(onDeletePreset)
                    presetToDelete = null
                }) { Text("Ta bort") }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) { Text("Avbryt") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                text = if (isEnglish) "Matkortet" else "Matkortet",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(58.dp))

            Text(
                text = if (isEnglish) "Current balance" else "Nuvarande saldo",
                style = MaterialTheme.typography.headlineSmall,
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${uiState.currentBalance} kr",
                fontSize = 74.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEnglish) "Daily budget: ${uiState.dailyAvailable} kr" else "Daglig budget: ${uiState.dailyAvailable} kr",
                style = MaterialTheme.typography.headlineSmall,
                color = textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                listOf(50, 70, 90).forEach { amount ->
                    Button(
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            pendingAmount = amount
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBg,
                            contentColor = Color.White
                        )
                    ) {
                        Text("$amount kr", fontSize = 22.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val digits = input.filter(Char::isDigit).take(2)
                        val parsed = digits.toIntOrNull()
                        if (digits.isEmpty() || (parsed != null && parsed <= 90)) {
                            amountText = digits
                        }
                    },
                    placeholder = {
                            Text(
                        if (isEnglish) "Enter amount" else "Ange belopp",
                        color = if (!darkUi) Color(0x99101114) else Color(0xCCFFFFFF),
                        fontSize = 18.sp
                    )
                },
                trailingIcon = {
                    Text(
                        "kr",
                        color = if (!darkUi) Color(0x99101114) else Color(0xCCFFFFFF),
                        fontSize = 16.sp
                    )
                },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                )

                Button(
                    onClick = {
                        val amount = amountText.toIntOrNull()
                        if (amount != null && amount in 1..90) {
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        pendingAmount = amount
                        amountText = ""
                    }
                },
                    enabled = isQuickAmountValid,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = addBg,
                        disabledContainerColor = addBg.copy(alpha = 0.35f),
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .width(86.dp)
                        .height(95.dp)
                ) {
                    Text(if (isEnglish) "Buy" else "Köp", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isEnglish) "Presets" else "Presets",
                color = textSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(presets) { preset ->
                    Surface(
                        color = presetBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                pendingAmount = preset.amount
                            },
                            onLongClick = {
                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                presetToDelete = preset
                            }
                        )
                    ) {
                        Text(
                            text = "${preset.label} ${preset.amount} kr",
                            color = textPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                item {
                    IconButton(
                        onClick = { showAddPresetDialog = true },
                        modifier = Modifier.background(presetBg, RoundedCornerShape(16.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Lägg till preset", tint = textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
