package com.example.vgy_matkort.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Holiday
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.vgy_matkort.ui.theme.*
import com.example.vgy_matkort.ui.components.AddHolidayDialog

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
            title = { Text("Återställ saldo", fontWeight = FontWeight.Bold) },
            text = { Text("Är du säker på att du vill återställa ditt saldo till 0 kr? Detta lägger till en korrigeringstransaktion i din historik.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetBalance()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRed)
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Inställningar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextWhite
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka", tint = TextWhite)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Haptic Feedback Card
            SettingCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Haptisk feedback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Vibration vid knapptryck",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 13.sp,
                            color = TextTertiary
                        )
                    }
                    Switch(
                        checked = isHapticEnabled,
                        onCheckedChange = {
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleHaptic(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryBlue,
                            checkedTrackColor = PrimaryBlue.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = SurfaceDark.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            // Holidays Card
            SettingCard {
                Column {
                    Text(
                        "Lov",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Hantera skollov och lediga dagar",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToHolidays()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue.copy(alpha = 0.2f),
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Text(
                            "Hantera lov",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Data Management Card
            SettingCard {
                Column {
                    Text(
                        "Saldo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Hantera ditt kortsaldo",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSetBalanceDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = TextWhite
                        )
                    ) {
                        Text(
                            "Ange nuvarande saldo",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showResetDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "Återställ saldo till 0 kr",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
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
    val haptic = LocalHapticFeedback.current

    if (showAddDialog) {
        AddHolidayDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { start, end, name ->
                onAddHoliday(start, end, name)
                showAddDialog = false
            },
            isHapticEnabled = isHapticEnabled
        )
    }
    
    if (holidayToDelete != null) {
        AlertDialog(
            onDismissRequest = { holidayToDelete = null },
            title = { Text("Ta bort lov", fontWeight = FontWeight.Bold) },
            text = { Text("Är du säker på att du vill ta bort '${holidayToDelete?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        holidayToDelete?.let { onDeleteHoliday(it) }
                        holidayToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRed)
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
            title = { Text("Import resultat", fontWeight = FontWeight.Bold) },
            text = { Text(importMessage!!) },
            confirmButton = {
                TextButton(onClick = { importMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Hantera lov",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextWhite
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = {
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                                strokeWidth = 2.dp,
                                color = TextWhite
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Importera lov från VGY", tint = TextWhite)
                        }
                    }
                    IconButton(onClick = { 
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Lägg till lov", tint = TextWhite)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Skollov",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Dagar inom dessa perioder räknas INTE som skoldagar.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = TextTertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
    val dateFormat = DateTimeFormatter.ofPattern("d MMM")
    val start = Instant.ofEpochMilli(holiday.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val end = Instant.ofEpochMilli(holiday.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val dateRange = "${start.format(dateFormat)} - ${end.format(dateFormat)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = holiday.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = TextTertiary
                )
            }
            IconButton(onClick = {
                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Ta bort",
                    tint = AccentRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Old AddHolidayDialog removed, using component instead

