package com.example.vgy_matkort.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.vgy_matkort.ui.theme.BackgroundGradientEnd
import com.example.vgy_matkort.ui.theme.BackgroundGradientStart
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Hem", Icons.Default.Home)
    object History : Screen("history", "Historik", Icons.Default.List)
    object Stats : Screen("stats", "Statistik", Icons.Default.DateRange)
    object Settings : Screen("settings", "Inställningar", Icons.Default.Settings)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    currentTheme: com.example.vgy_matkort.ui.theme.AppTheme,
    onSetTheme: (com.example.vgy_matkort.ui.theme.AppTheme) -> Unit
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val shouldShowTutorial by viewModel.shouldShowTutorial.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()


    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Stats,
        Screen.Settings
    )

    // Global Tutorial Overlay
    val tutorialStep by viewModel.tutorialStep.collectAsState()
    val highlightRegistry by viewModel.highlightRegistry.collectAsState()
    
    // Calculate current highlight specs based on step
    val currentStepData = TutorialStepData.steps.getOrNull(tutorialStep)
    val currentHighlightSpecs = remember(tutorialStep, highlightRegistry) {
        val area = currentStepData?.highlightArea
        if (area != null) {
            val rect = highlightRegistry[area.key]
            if (rect != null) {
                // Inflate the rect by 8.dp for better visual breathing room
                val inflation = 16f // approx 8.dp in pixels, but we need density. 
                // Let's use a fixed inflation or get density. 
                // Since we are in a Composable, we can use LocalDensity.
                // But wait, we are inside remember block.
                // I'll just inflate it by a safe amount or use LocalDensity outside.
                listOf(HighlightSpec(rect.inflate(16f), 16.dp)) 
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }



    val gradientColors = com.example.vgy_matkort.ui.theme.LocalGradientColors.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    // Create pager state for main screens (excluding manage_holidays)
    val pagerState = rememberPagerState(pageCount = { items.size })
    
    // Sync pager with navigation
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val targetRoute = items[pagerState.currentPage].route
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (targetRoute != currentRoute) {
                if (isHapticEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientColors.start, gradientColors.mid, gradientColors.end)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent, // Or semi-transparent
                    contentColor = Color.White
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            onClick = {
                                if (isHapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = if (screen == Screen.Settings) {
                                Modifier.onGloballyPositioned { 
                                    viewModel.registerHighlight("settings_nav_item", it.boundsInRoot())
                                }
                            } else {
                                Modifier
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            // HorizontalPager for swipeable main screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> HomeScreen(
                        uiState = uiState,
                        transactions = transactions,
                        presets = presets,
                        onAddTransaction = viewModel::addTransaction,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onAddPreset = viewModel::addPreset,
                        onDeletePreset = viewModel::deletePreset,
                        onNavigateToWeeklySummary = { /* TODO */ },
                        onNavigateToSettings = { navController.navigate("settings") },
                        shouldShowTutorial = shouldShowTutorial,
                        currentTutorialStep = tutorialStep,
                        onTutorialComplete = viewModel::markTutorialAsSeen,
                        onShowTutorial = viewModel::showTutorial,
                        onRegisterHighlight = viewModel::registerHighlight,
                        isHapticEnabled = isHapticEnabled
                    )
                    1 -> HistoryScreen(
                        transactions = transactions,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onRegisterHighlight = viewModel::registerHighlight,
                        isHapticEnabled = isHapticEnabled
                    )
                    2 -> StatsScreen(
                        uiState = uiState,
                        transactions = transactions,
                        onRegisterHighlight = viewModel::registerHighlight
                    )
                    3 -> SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateBack = { navController.popBackStack() },
                        periodBudgetRemaining = uiState.periodBudgetRemaining,
                        onSetPeriodBudget = viewModel::setPeriodBudgetRemaining,
                        onRegisterHighlight = viewModel::registerHighlight,
                        onNavigateToHolidays = { navController.navigate("manage_holidays") },
                        isHapticEnabled = isHapticEnabled,
                        onToggleHaptic = viewModel::toggleHaptic,
                        currentTheme = currentTheme,
                        onSetTheme = onSetTheme
                    )
                }
            }
            
            // Keep NavHost for sub-screens like manage_holidays
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { /* Handled by pager */ }
                composable(Screen.History.route) { /* Handled by pager */ }
                composable(Screen.Stats.route) { /* Handled by pager */ }
                composable(Screen.Settings.route) { /* Handled by pager */ }
                composable("manage_holidays") {
                    ManageHolidaysScreen(
                        holidays = holidays,
                        onAddHoliday = viewModel::addHoliday,
                        onDeleteHoliday = viewModel::deleteHoliday,
                        onImportHolidays = viewModel::importHolidaysFromWeb,
                        onBack = { navController.popBackStack() },
                        isHapticEnabled = isHapticEnabled
                    )
                }
            }
        }
        
        // Tutorial Overlay
        if (shouldShowTutorial && currentStepData != null) {
            // Auto-navigation based on step
            LaunchedEffect(currentStepData.route) {
                if (currentStepData.route == "settings") {
                     navController.navigate("settings") {
                        launchSingleTop = true
                     }
                } else {
                    navController.navigate(currentStepData.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            
            TutorialOverlayWithDim(
                currentStep = tutorialStep,
                totalSteps = TutorialStepData.steps.size,
                title = currentStepData.title,
                description = currentStepData.description,
                highlightSpecs = currentHighlightSpecs,
                onNext = viewModel::nextTutorialStep,
                onBack = viewModel::prevTutorialStep,
                onSkip = viewModel::markTutorialAsSeen
            )
        }
    }
}
