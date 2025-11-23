package com.example.vgy_matkort.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Preset
import com.example.vgy_matkort.data.Transaction
import com.example.vgy_matkort.ui.theme.BackgroundGradientEnd
import com.example.vgy_matkort.ui.theme.BackgroundGradientStart
import com.example.vgy_matkort.ui.theme.SurfaceDark
import com.example.vgy_matkort.ui.theme.TextSecondary
import com.example.vgy_matkort.ui.theme.TextWhite

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

    shouldShowTutorial: Boolean,
    currentTutorialStep: Int,
    onTutorialComplete: () -> Unit,
    onShowTutorial: () -> Unit,
    onRegisterHighlight: (String, Rect) -> Unit,
    isHapticEnabled: Boolean
) {
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }
    var showAddPresetDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val tankViewPagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 })

    // Sync TankView pager with tutorial step
    LaunchedEffect(currentTutorialStep) {
        if (shouldShowTutorial) {
            val stepData = TutorialStepData.steps.getOrNull(currentTutorialStep)
            stepData?.tankViewPage?.let { page ->
                tankViewPagerState.animateScrollToPage(page)
            }
        }
    }

    if (showAddPresetDialog) {
        com.example.vgy_matkort.ui.components.AddPresetDialog(
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundGradientStart, BackgroundGradientEnd)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { }, // Empty title for cleaner look
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        IconButton(
                            onClick = {
                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onShowTutorial()
                            },
                            modifier = Modifier.onGloballyPositioned { 
                                onRegisterHighlight("settings_theme", it.boundsInRoot())
                            }
                        ) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = "Hjälp / Tutorial",
                                tint = TextWhite
                            )
                        }
                    }
                )
            },
            floatingActionButton = {}
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Balance Section (Replaces TankView)
                Box(modifier = Modifier.onGloballyPositioned { 
                    onRegisterHighlight("tank_view", it.boundsInRoot())
                }) {
                    BalanceSection(
                        uiState = uiState,
                        pagerState = tankViewPagerState
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Quick Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .onGloballyPositioned {
                            onRegisterHighlight("quick_add_buttons", it.boundsInRoot())
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(
                        value = 50,
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddTransaction(50)
                        }
                    )
                    QuickActionButton(
                        value = 70,
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddTransaction(70)
                        }
                    )
                    QuickActionButton(
                        value = 90,
                        onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddTransaction(90)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Presets Header
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                )

                // Presets List
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            onRegisterHighlight("presets_list", it.boundsInRoot())
                        },
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(presets) { preset ->
                        PresetChip(
                            preset = preset,
                            onClick = {
                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddTransaction(preset.amount)
                            },
                            onLongClick = {
                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                presetToDelete = preset
                            }
                        )
                    }
                    item {
                        AddPresetChip(onClick = {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showAddPresetDialog = true
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BalanceSection(
    uiState: UiState,
    pagerState: androidx.compose.foundation.pager.PagerState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp
        ) { page ->
            when (page) {
                0 -> BalanceCard(
                    title = "Tillgängligt nu",
                    amount = uiState.currentBalance,
                    subtitle = "Nuvarande saldo",
                    highlight = true
                )
                1 -> BalanceCard(
                    title = "Daglig budget",
                    amount = uiState.dailyAvailable,
                    subtitle = "${uiState.daysRemaining} skoldagar kvar"
                )
                2 -> BalanceCard(
                    title = "Denna vecka",
                    amount = uiState.currentWeekBalance,
                    subtitle = "Ackumulerat: ${uiState.currentWeekAccumulated} kr"
                )
                3 -> BalanceCard(
                    title = "Periodens budget",
                    amount = uiState.periodBudgetRemaining,
                    subtitle = "Kvar till lovet"
                )
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
                val color = if (pagerState.currentPage == iteration) TextWhite else TextWhite.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun BalanceCard(
    title: String,
    amount: Int,
    subtitle: String,
    highlight: Boolean = false
) {
    // Transparent - showing gradient background
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$amount kr",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp
            ),
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun QuickActionButton(
    value: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = SurfaceDark,
        modifier = Modifier.size(100.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetChip(preset: Preset, onClick: () -> Unit, onLongClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(80.dp)
            .width(120.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = preset.label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${preset.amount} kr",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@Composable
fun AddPresetChip(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(80.dp)
            .width(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.3f))
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Lägg till",
                tint = TextWhite
            )
        }
    }
}

