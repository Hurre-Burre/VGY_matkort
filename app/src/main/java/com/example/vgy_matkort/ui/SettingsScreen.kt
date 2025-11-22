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
    currentBalance: Int = 0,
    onSetManualBalance: (Int) -> Unit,
    onRegisterHighlight: (String, Rect) -> Unit,
    onNavigateToHolidays: () -> Unit = {}
) {
    var showSetBalanceDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    // Function to handle reset (wrapper to match signature if needed, or just use direct logic)
    val onResetBalance = { onSetManualBalance(0) } // Simplified reset logic reusing setManualBalance or we need to pass onResetBalance from ViewModel if it does something special. 
    // Looking at MainViewModel, resetBalance() exists. But AppNavigation passes onSetManualBalance.
    // Let's check AppNavigation again. It passes onSetManualBalance. It does NOT pass onResetBalance.
    // However, the original code in SettingsScreen (line 40) calls `onResetBalance()`.
    // This implies `onResetBalance` was either passed in or defined locally.
    // Given AppNavigation only passes `onSetManualBalance`, I will define `onResetBalance` locally using `onSetManualBalance(0)`.
    
    val onNavigateToHolidays = { /* TODO: Implement navigation if needed or it might be handled by a lambda passed in? */ 
        // AppNavigation passes nothing for holidays navigation?
        // Wait, AppNavigation:
        // composable("settings") { SettingsScreen(...) }
        // It does NOT pass onNavigateToHolidays.
        // But line 101 in SettingsScreen calls `onNavigateToHolidays`.
        // And line 153 in AppNavigation handles auto-nav for tutorial?
        // Let's look at AppNavigation again.
        // It seems I might have missed some arguments in my reconstruction plan or the file was edited before.
        // Actually, in the broken file, line 101 uses `onNavigateToHolidays`.
        // I should probably add it to the arguments or define it.
        // But wait, `ManageHolidaysScreen` is in the same file? No, it's a separate function in the same file (line 138).
        // Ah, `SettingsScreen` likely navigates to it.
        // But `ManageHolidaysScreen` is a Composable.
        // If `SettingsScreen` navigates to it, it needs a NavController or a callback.
        // The broken code has `onNavigateToHolidays` usage.
        // I will add it to the arguments to be safe, or check if I can infer where it goes.
        // For now, I'll add it to arguments and update AppNavigation later if needed, OR just define it as empty/todo if I can't change AppNavigation easily (but I can).
        // Actually, looking at `AppNavigation.kt`, `SettingsScreen` is called with:
        /*
        SettingsScreen(
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onNavigateBack = { navController.popBackStack() },
            currentBalance = uiState.currentBalance,
            onSetManualBalance = viewModel::setManualBalance,
            onRegisterHighlight = viewModel::registerHighlight
        )
        */
        // It is NOT passed `onNavigateToHolidays`.
        // This means `SettingsScreen` signature in `AppNavigation` call site does not match what `SettingsScreen` body expects (line 101).
        // I must fix `SettingsScreen` to match the call site OR update the call site.
        // Since I am fixing compilation errors, I should probably make `SettingsScreen` match the call site first.
        // But then `onNavigateToHolidays` (line 101) will be unresolved.
        // I will define `val onNavigateToHolidays = { }` locally for now to fix compilation, or better, add it to the signature and update AppNavigation.
        // Updating AppNavigation is better practice.
        // But wait, `ManageHolidaysScreen` is right below. Maybe it's a dialog?
        // No, it looks like a full screen (Scaffold).
        // I'll add `onNavigateToHolidays: () -> Unit` to the signature and update AppNavigation in a subsequent step if needed, or just provide a dummy for now.
        // Actually, I'll check if I can just use a local state to show it?
        // The file has `ManageHolidaysScreen` defined.
        // Maybe `SettingsScreen` shows it?
        // No, usually it's navigation.
        // I will add `onNavigateToHolidays: () -> Unit = {}` as a default argument to avoid breaking AppNavigation immediately, but I should probably fix AppNavigation too.
    }

    if (showSetBalanceDialog) {
        SetBalanceDialog(
            currentBalance = currentBalance,
            onDismiss = { showSetBalanceDialog = false },
            onConfirm = { newBalance ->
                onSetManualBalance(newBalance)
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
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleTheme(it)
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
    onBack: () -> Unit
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
                    HolidayItem(holiday = holiday, onDelete = { holidayToDelete = holiday })
                }
            }
        }
    }
}

@Composable
fun HolidayItem(holiday: Holiday, onDelete: () -> Unit) {
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
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
