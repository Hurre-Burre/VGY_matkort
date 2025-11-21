package com.example.vgy_matkort.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Preset
import com.example.vgy_matkort.data.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UiState,
    transactions: List<Transaction>,
    presets: List<Preset>,
    onAddTransaction: (Int) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onAddPreset: (Int, String) -> Unit,
    onDeletePreset: (Preset) -> Unit,
    onNavigateToWeeklySummary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSetManualBalance: (Int) -> Unit
) {
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }
    var showSetBalanceDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }

    if (showAddPresetDialog) {
        AddPresetDialog(
            onDismiss = { showAddPresetDialog = false },
            onConfirm = { amount, label ->
                onAddPreset(amount, label)
                showAddPresetDialog = false
            }
        )
    }
    
    if (presetToDelete != null) {
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Ta bort förinställning") },
            text = { Text("Är du säker på att du vill ta bort '${presetToDelete?.label}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        presetToDelete?.let { onDeletePreset(it) }
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ta bort")
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("Avbryt")
                }
            }
        )
    }
    
    if (showSetBalanceDialog) {
        SetBalanceDialog(
            currentBalance = uiState.currentBalance,
            onDismiss = { showSetBalanceDialog = false },
            onConfirm = { newBalance ->
                onSetManualBalance(newBalance)
                showSetBalanceDialog = false
            }
        )
    }
    
    if (showTutorialDialog) {
        TutorialDialog(
            onDismiss = { showTutorialDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { showSetBalanceDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Ange saldo")
                    }
                    IconButton(onClick = { showTutorialDialog = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Hjälp")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Inställningar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddPresetDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Preset")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tank / Gauge View
            TankView(
                dailyAvailable = uiState.dailyAvailable,
                currentBalance = uiState.currentBalance,
                daysRemaining = uiState.daysRemaining,
                periodEnd = uiState.periodEnd,
                currentWeekBalance = uiState.currentWeekBalance,
                currentWeekAccumulated = uiState.currentWeekAccumulated,
                periodBudgetRemaining = uiState.periodBudgetRemaining,
                totalPeriodBudget = uiState.totalPeriodBudget
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Add Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddButton(amount = 50, onClick = { onAddTransaction(50) }, modifier = Modifier.weight(1f))
                QuickAddButton(amount = 70, onClick = { onAddTransaction(70) }, modifier = Modifier.weight(1f))
                QuickAddButton(amount = 90, onClick = { onAddTransaction(90) }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Presets
            if (presets.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        PresetChip(
                            preset = preset,
                            onClick = { onAddTransaction(preset.amount) },
                            onLongClick = { presetToDelete = preset }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TankView(
    dailyAvailable: Int, 
    currentBalance: Int, 
    daysRemaining: Int, 
    periodEnd: java.time.LocalDate?,
    currentWeekBalance: Int,
    currentWeekAccumulated: Int,
    periodBudgetRemaining: Int,
    totalPeriodBudget: Int
) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 })
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.pager.HorizontalPager(state = pagerState) { page ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> { // Available Now
                            GaugeView(
                                value = currentBalance,
                                max = 500,
                                min = -200,
                                label = "Total"
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Tillgängligt nu", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        text = "$currentBalance kr",
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            currentBalance >= 200 -> Color(0xFF4CAF50)
                                            currentBalance >= 100 -> Color(0xFF8BC34A)
                                            currentBalance >= 0 -> Color(0xFFFFC107)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                    Text(
                                        text = "Nuvarande saldo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        1 -> { // Smart Daily
                            GaugeView(
                                value = dailyAvailable,
                                max = 140,
                                min = 0,
                                label = "Daily"
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Daglig budget", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        text = "$dailyAvailable kr",
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            dailyAvailable >= 90 -> Color(0xFF4CAF50)
                                            dailyAvailable >= 70 -> Color(0xFF8BC34A)
                                            dailyAvailable >= 50 -> Color(0xFFFFC107)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                    Text(
                                        text = "$daysRemaining skoldagar kvar",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        2 -> { // This Week
                            GaugeView(
                                value = currentWeekBalance,
                                max = 200,
                                min = -200,
                                label = "Week"
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Denna vecka", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        text = "$currentWeekBalance kr",
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            currentWeekBalance >= 50 -> Color(0xFF4CAF50)
                                            currentWeekBalance >= 0 -> Color(0xFF8BC34A)
                                            currentWeekBalance >= -50 -> Color(0xFFFFC107)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                    Text(
                                        text = "Ackumulerat: $currentWeekAccumulated kr",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        3 -> { // Period Budget
                            GaugeView(
                                value = periodBudgetRemaining,
                                max = totalPeriodBudget,
                                min = 0,
                                label = "Period"
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Periodens budget", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        text = "$periodBudgetRemaining kr",
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            periodBudgetRemaining >= (totalPeriodBudget * 0.6) -> Color(0xFF4CAF50)
                                            periodBudgetRemaining >= (totalPeriodBudget * 0.4) -> Color(0xFF8BC34A)
                                            periodBudgetRemaining >= (totalPeriodBudget * 0.2) -> Color(0xFFFFC107)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                    Text(
                                        text = "Kvar till lovet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pager Indicator
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GaugeView(
    value: Int, 
    max: Int, 
    min: Int, 
    label: String,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.size(350.dp, 250.dp) // Increased height for more vertical space
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Arc Background
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 40f, cap = StrokeCap.Round),
                size = Size(size.width, size.width), // Full width circle (350x350)
                topLeft = Offset(0f, 0f)
            )
            
            // Calculate progress
            val range = max - min
            val normalizedValue = (value - min).coerceIn(0, range)
            val progress = (normalizedValue / range.toFloat()) * 180f
            
            val gaugeColor = when {
                value >= (max * 0.6) -> Color(0xFF4CAF50)
                value >= (max * 0.4) -> Color(0xFF8BC34A)
                value >= (max * 0.2) -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }

            drawArc(
                color = gaugeColor,
                startAngle = 180f,
                sweepAngle = progress,
                useCenter = false,
                style = Stroke(width = 40f, cap = StrokeCap.Round),
                size = Size(size.width, size.width),
                topLeft = Offset(0f, 0f)
            )
        }
        
        // Content inside the gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 40.dp), // Push text down
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetChip(preset: Preset, onClick: () -> Unit, onLongClick: () -> Unit) {
    Surface(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Text(
            text = "${preset.label} (${preset.amount}kr)",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun AddPresetDialog(onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lägg till förinställning") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Belopp (kr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Etikett (t.ex. Mellanmål)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountInt = amount.toIntOrNull()
                    if (amountInt != null && label.isNotBlank()) {
                        onConfirm(amountInt, label)
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

@Composable
fun QuickAddButton(amount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = "$amount kr",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
