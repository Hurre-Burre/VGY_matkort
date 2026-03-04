package com.example.vgy_matkort.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import com.example.vgy_matkort.ui.components.AddHolidayDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    periodBudgetRemaining: Int = 0,
    onSetPeriodBudget: (Int) -> Unit,
    onNavigateToHolidays: () -> Unit = {},
    isHapticEnabled: Boolean,
    onToggleHaptic: (Boolean) -> Unit,
    currentTheme: com.example.vgy_matkort.ui.theme.AppTheme,
    onSetTheme: (com.example.vgy_matkort.ui.theme.AppTheme) -> Unit
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
                    colors = ButtonDefaults.textButtonColors(contentColor = iOSRed)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(iOSBackground)
    ) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Inställningar",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = iOSTextBlack
                    )
                }

                // Theme Selector Card
                SettingCard {
                    Column {
                        Text(
                            "Tema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = iOSTextBlack
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            com.example.vgy_matkort.ui.theme.AppTheme.values().forEach { theme ->
                                val color = when (theme) {
                                    com.example.vgy_matkort.ui.theme.AppTheme.Blue -> com.example.vgy_matkort.ui.theme.BluePrimary
                                    com.example.vgy_matkort.ui.theme.AppTheme.Green -> com.example.vgy_matkort.ui.theme.GreenPrimary
                                    com.example.vgy_matkort.ui.theme.AppTheme.Red -> com.example.vgy_matkort.ui.theme.RedPrimary
                                    com.example.vgy_matkort.ui.theme.AppTheme.Orange -> com.example.vgy_matkort.ui.theme.OrangePrimary
                                    com.example.vgy_matkort.ui.theme.AppTheme.Purple -> com.example.vgy_matkort.ui.theme.PurplePrimary
                                    com.example.vgy_matkort.ui.theme.AppTheme.Pink -> com.example.vgy_matkort.ui.theme.PinkPrimary
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(color)
                                            .clickable { 
                                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onSetTheme(theme) 
                                            }
                                            .then(
                                                if (currentTheme == theme) {
                                                    Modifier.border(2.dp, iOSBlue, androidx.compose.foundation.shape.CircleShape)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = theme.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (currentTheme == theme) iOSTextBlack else iOSTextGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

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
                                color = iOSTextBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Vibration vid knapptryck",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color = iOSTextGray
                            )
                        }
                        Switch(
                            checked = isHapticEnabled,
                            onCheckedChange = {
                                if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleHaptic(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = iOSBlue,
                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = iOSTextLightGray
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
                            color = iOSTextBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Hantera skollov och lediga dagar",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 13.sp,
                            color = iOSTextGray
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
                                containerColor = iOSBlue.copy(alpha = 0.1f),
                                contentColor = iOSBlue
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
                            color = iOSTextBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Hantera ditt kortsaldo",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 13.sp,
                            color = iOSTextGray
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
                                containerColor = iOSBlue,
                                contentColor = androidx.compose.ui.graphics.Color.White
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
                                contentColor = iOSRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, iOSRed.copy(alpha = 0.5f))
                        ) {
                            Text(
                                "Återställ saldo till 0 kr",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
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
                    colors = ButtonDefaults.textButtonColors(contentColor = iOSRed)
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
        containerColor = iOSBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Hantera lov",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = iOSTextBlack
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = {
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka", tint = iOSBlue)
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
                                color = iOSBlue
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Importera lov från VGY", tint = iOSBlue)
                        }
                    }
                    IconButton(onClick = { 
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Lägg till lov", tint = iOSBlue)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Skollov",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = iOSTextBlack,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Dagar inom dessa perioder räknas INTE som skoldagar.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = iOSTextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
    val dateFormatWithYear = DateTimeFormatter.ofPattern("d MMM yyyy")
    val start = Instant.ofEpochMilli(holiday.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val end = Instant.ofEpochMilli(holiday.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
    
    // Smart date range formatting: show year only once if same year
    val dateRange = if (start.year == end.year) {
        "${start.format(dateFormat)} - ${end.format(dateFormatWithYear)}"
    } else {
        "${start.format(dateFormatWithYear)} - ${end.format(dateFormatWithYear)}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
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
                    color = iOSTextBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = iOSTextGray
                )
            }
            IconButton(onClick = {
                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Ta bort",
                    tint = iOSRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Old AddHolidayDialog removed, using component instead

