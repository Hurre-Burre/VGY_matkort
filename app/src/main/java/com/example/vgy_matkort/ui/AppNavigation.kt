package com.example.vgy_matkort.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Hem", Icons.Default.Home)
    object History : Screen("history", "Historik", Icons.Default.List)
    object Stats : Screen("stats", "Statistik", Icons.Default.DateRange)
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val shouldShowTutorial by viewModel.shouldShowTutorial.collectAsState()


    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Stats
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
                listOf(HighlightSpec(rect, 16.dp)) // Default corner radius
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
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
                        onRegisterHighlight = viewModel::registerHighlight
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        transactions = transactions,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onRegisterHighlight = viewModel::registerHighlight
                    )
                }
                composable(Screen.Stats.route) {
                    StatsScreen(
                        uiState = uiState,
                        transactions = transactions,
                        onRegisterHighlight = viewModel::registerHighlight
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateBack = { navController.popBackStack() },
                        currentBalance = uiState.currentBalance,
                        onSetManualBalance = viewModel::setManualBalance,
                        onRegisterHighlight = viewModel::registerHighlight,
                        onNavigateToHolidays = { navController.navigate("manage_holidays") }
                    )
                }
                composable("manage_holidays") {
                    ManageHolidaysScreen(
                        holidays = holidays,
                        onAddHoliday = viewModel::addHoliday,
                        onDeleteHoliday = viewModel::deleteHoliday,
                        onImportHolidays = viewModel::importHolidaysFromWeb,
                        onBack = { navController.popBackStack() }
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
