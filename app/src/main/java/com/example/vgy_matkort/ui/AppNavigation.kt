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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
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

    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Stats
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Only show bottom bar on main screens
            val isMainScreen = items.any { it.route == currentDestination?.route }
            
            if (isMainScreen) {
                NavigationBar {
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
                    onNavigateToWeeklySummary = { navController.navigate("weekly_summary") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onSetManualBalance = viewModel::setManualBalance
                )
            }
            
            composable(Screen.History.route) {
                HistoryScreen(
                    transactions = transactions,
                    onDeleteTransaction = viewModel::deleteTransaction
                )
            }
            
            composable(Screen.Stats.route) {
                StatsScreen(
                    uiState = uiState,
                    transactions = transactions
                )
            }
            
            composable("weekly_summary") {
                WeeklySummaryScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onNavigateToHolidays = { navController.navigate("manage_holidays") },
                    onResetBalance = viewModel::resetBalance,
                    onBack = { navController.popBackStack() }
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
}
