package com.example.vgy_matkort.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vgy_matkort.data.Holiday
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    periodBudgetRemaining: Int = 0,
    onSetPeriodBudget: (Int) -> Unit,
    onRegisterHighlight: (String, Rect) -> Unit,
    onNavigateToHolidays: () -> Unit = {},
    isHapticEnabled: Boolean,
    onToggleHaptic: (Boolean) -> Unit
) {

    var showSetBalanceDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val onResetBalance = { onSetPeriodBudget(0) }

    if (showSetBalanceDialog) {
        SetBalanceDialog(
            currentBalance = periodBudgetRemaining,
            onDismiss = { showSetBalanceDialog = false },
            onConfirm = { newBalance ->
                onSetPeriodBudget(newBalance)
                showSetBalanceDialog = false
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Återställ saldo") },
            text = { Text("Är du säker på att du vill återställa ditt saldo till 0 kr? Detta lägger till en korrigeringstransaktion i din historik.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetBalance()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Återställ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inställningar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    onRegisterHighlight("settings_screen", coordinates.boundsInRoot())
                }
                .padding(16.dp)
        ) {
            // Appearance Section
            Text(
                "Utseende",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mörkt läge")
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = {
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleTheme(it)
                    }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Haptic Feedback Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Haptisk feedback")
                Switch(
                    checked = isHapticEnabled,
                    onCheckedChange = {
                        if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleHaptic(it)
                    }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Holidays Section
            Text(
                "Lov",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedButton(
                onClick = {
                    if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToHolidays()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hantera lov")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Data Section
            Text(
                "Data",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = {
                    if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showSetBalanceDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Ange nuvarande saldo")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showResetDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Återställ saldo till 0 kr")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHolidaysScreen(
    holidays: List<Holiday>,
    onAddHoliday: (Long, Long, String) -> Unit,
    onDeleteHoliday: (Holiday) -> Unit,
    onImportHolidays: suspend () -> Result<Int>,
    onBack: () -> Unit,
    isHapticEnabled: Boolean
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var holidayToDelete by remember { mutableStateOf<Holiday?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (showAddDialog) {
        AddHolidayDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { start, end, name ->
                onAddHoliday(start, end, name)
                showAddDialog = false
            }
        )
    }
    
    if (holidayToDelete != null) {
        AlertDialog(
            onDismissRequest = { holidayToDelete = null },
            title = { Text("Ta bort lov") },
            text = { Text("Är du säker på att du vill ta bort '${holidayToDelete?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        holidayToDelete?.let { onDeleteHoliday(it) }
                        holidayToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ta bort")
                }
            },
            dismissButton = {
                TextButton(onClick = { holidayToDelete = null }) {
                    Text("Avbryt")
                }
            }
        )
    }
    
    if (importMessage != null) {
        AlertDialog(
            onDismissRequest = { importMessage = null },
            title = { Text("Import resultat") },
            text = { Text(importMessage!!) },
            confirmButton = {
                TextButton(onClick = { importMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hantera lov") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isImporting = true
                            scope.launch {
                                val result = onImportHolidays()
                                isImporting = false
                                result.fold(
                                    onSuccess = { count ->
                                        importMessage = if (count > 0) {
                                            "Importerade $count nya lov från Värmdö Gymnasium"
                                        } else {
                                            "Inga nya lov att importera. Alla lov är redan tillagda."
                                        }
                                    },
                                    onFailure = { error ->
                                        importMessage = "Kunde inte importera lov: ${error.message}"
                                    }
                                )
                            }
                        },
                        enabled = !isImporting
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Importera lov från VGY")
                        }
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Lägg till lov")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Skollov",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Dagar inom dessa perioder räknas INTE som skoldagar.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(holidays) { holiday ->
                    HolidayItem(
                        holiday = holiday, 
                        onDelete = { holidayToDelete = holiday },
                        isHapticEnabled = isHapticEnabled
                    )
                }
            }
        }
    }
}

@Composable
fun HolidayItem(holiday: Holiday, onDelete: () -> Unit, isHapticEnabled: Boolean) {
    val haptic = LocalHapticFeedback.current
    val dateFormat = DateTimeFormatter.ofPattern("MMM dd")
    val start = Instant.ofEpochMilli(holiday.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val end = Instant.ofEpochMilli(holiday.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val dateRange = "${start.format(dateFormat)} - ${end.format(dateFormat)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = holiday.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = dateRange, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = {
                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Ta bort")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHolidayDialog(onDismiss: () -> Unit, onConfirm: (Long, Long, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var startDateStr by remember { mutableStateOf("") }
    var endDateStr by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lägg till lov") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Namn (t.ex. Sportlov)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = startDateStr,
                    onValueChange = { 
                        startDateStr = it
                        isError = false
                    },
                    label = { Text("Startdatum (ÅÅÅÅ-MM-DD)") },
                    placeholder = { Text("2025-10-27") },
                    isError = isError
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = endDateStr,
                    onValueChange = { 
                        endDateStr = it
                        isError = false
                    },
                    label = { Text("Slutdatum (ÅÅÅÅ-MM-DD)") },
                    placeholder = { Text("2025-10-31") },
                    isError = isError
                )
                if (isError) {
                    Text(
                        text = "Ogiltigt datumformat. Använd ÅÅÅÅ-MM-DD",
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
                    try {
                        val start = LocalDate.parse(startDateStr).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val end = LocalDate.parse(endDateStr).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        if (name.isNotBlank()) {
                            onConfirm(start, end, name)
                        } else {
                            isError = true
                        }
                    } catch (e: Exception) {
                        isError = true
                    }
                }
            ) {
                Text("Lägg till")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
}
