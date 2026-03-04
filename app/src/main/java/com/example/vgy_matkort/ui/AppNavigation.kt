package com.example.vgy_matkort.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    val uiState = viewModel.uiState.collectAsState().value
    val transactions = viewModel.transactions.collectAsState().value
    val presets = viewModel.presets.collectAsState().value
    val holidays = viewModel.holidays.collectAsState().value
    val isHapticEnabled = viewModel.isHapticEnabled.collectAsState().value

    val items: List<Screen> = listOf(Screen.Home, Screen.History, Screen.Stats, Screen.Settings)

    val gradientColors = com.example.vgy_matkort.ui.theme.LocalGradientColors.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val targetRoute = items[pagerState.currentPage].route
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (targetRoute != currentRoute) {
                if (isHapticEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradientColors.start, gradientColors.mid, gradientColors.end)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = Color.Transparent, contentColor = Color.White) {
                    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                    items.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { destination -> destination.route == screen.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            onClick = {
                                if (isHapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(state = pagerState, modifier = Modifier.padding(innerPadding)) { page ->
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
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        isHapticEnabled = isHapticEnabled
                    )
                    1 -> HistoryScreen(
                        transactions = transactions,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        isHapticEnabled = isHapticEnabled
                    )
                    2 -> StatsScreen(
                        uiState = uiState,
                        transactions = transactions
                    )
                    3 -> SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateBack = { navController.popBackStack() },
                        periodBudgetRemaining = uiState.periodBudgetRemaining,
                        onSetPeriodBudget = viewModel::setPeriodBudgetRemaining,
                        onNavigateToHolidays = { navController.navigate("manage_holidays") },
                        isHapticEnabled = isHapticEnabled,
                        onToggleHaptic = viewModel::toggleHaptic,
                        currentTheme = currentTheme,
                        onSetTheme = onSetTheme
                    )
                }
            }

            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { }
                composable(Screen.History.route) { }
                composable(Screen.Stats.route) { }
                composable(Screen.Settings.route) { }
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
    }
}

private fun Rect.inflate(padding: Float): Rect {
    return Rect(
        left = left - padding,
        top = top - padding,
        right = right + padding,
        bottom = bottom + padding
    )
}
