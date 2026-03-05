package com.example.vgy_matkort.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.ui.theme.AppTheme
import kotlinx.coroutines.launch

sealed class Screen(val title: String, val icon: ImageVector) {
    data object Home : Screen("Home", Icons.Default.Home)
    data object History : Screen("History", Icons.AutoMirrored.Filled.List)
    data object Stats : Screen("Stats", Icons.Default.DateRange)
    data object Restaurants : Screen("Restaurants", Icons.Default.Edit)
    data object Settings : Screen("Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    currentTheme: AppTheme,
    onSetTheme: (AppTheme) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val setupPreferences by viewModel.setupPreferences.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()
    val language by viewModel.language.collectAsState()
    val reminderMinutes by viewModel.reminderMinutes.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val autoHolidaysEnabled by viewModel.autoHolidaysEnabled.collectAsState()
    val isEnglish = language == "en"

    if (!setupPreferences.isCompleted) {
        SetupScreen(
            onCompleteSetup = { balance, income, startDate, endDate, theme, reminder, notifications ->
                viewModel.completeSetup(balance, income, startDate, endDate, theme, reminder, notifications)
            },
            onNavigateToHolidays = {}
        )
        return
    }

    val items = listOf(Screen.Home, Screen.History, Screen.Stats, Screen.Restaurants, Screen.Settings)
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    val appBackground = when (currentTheme) {
        AppTheme.System -> {
            if (isDarkTheme) {
                Brush.verticalGradient(listOf(Color(0xFF191810), Color(0xFF232317), Color(0xFF1A2A1C)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFF5F2E8), Color(0xFFF0ECCF), Color(0xFFE6F2DE)))
            }
        }
        AppTheme.Signature -> Brush.verticalGradient(listOf(Color(0xFFF5F2E8), Color(0xFFF0ECCF), Color(0xFFE6F2DE)))
        AppTheme.Blue -> Brush.verticalGradient(listOf(Color(0xFF10213D), Color(0xFF1D4F91), Color(0xFF2E74CC)))
        AppTheme.Green -> Brush.verticalGradient(listOf(Color(0xFF163823), Color(0xFF2D7A4A), Color(0xFF42AA63)))
        AppTheme.Red -> Brush.verticalGradient(listOf(Color(0xFF3B1212), Color(0xFF8F2E2E), Color(0xFFCC4A4A)))
        AppTheme.Orange -> Brush.verticalGradient(listOf(Color(0xFF6F1E05), Color(0xFFBF4A17), Color(0xFFC2511E)))
        AppTheme.Purple -> Brush.verticalGradient(listOf(Color(0xFF2A173D), Color(0xFF5D33A1), Color(0xFF8158D1)))
        AppTheme.Pink -> Brush.verticalGradient(listOf(Color(0xFF3D162A), Color(0xFF9E3A72), Color(0xFFC95B96)))
    }
    val navBarContainer = when (currentTheme) {
        AppTheme.System -> if (isDarkTheme) Color(0xE629281D) else Color(0xEAF5F1E7)
        AppTheme.Signature -> Color(0xEAF5F1E7)
        AppTheme.Blue -> Color(0xCC0D203A)
        AppTheme.Green -> Color(0xCC123321)
        AppTheme.Red -> Color(0xCC3A1515)
        AppTheme.Orange -> Color(0xCC5E220B)
        AppTheme.Purple -> Color(0xCC2C1B40)
        AppTheme.Pink -> Color(0xCC3F1930)
    }
    val navTextColor = if ((currentTheme == AppTheme.System && !isDarkTheme) || currentTheme == AppTheme.Signature) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = navBarContainer,
                    tonalElevation = 3.5.dp
                ) {
                    items.forEachIndexed { index, screen ->
                        val selected = pagerState.currentPage == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                            label = {
                                Text(
                                    text = when (screen) {
                                        Screen.Home -> if (isEnglish) "Home" else "Hem"
                                        Screen.History -> if (isEnglish) "History" else "Historik"
                                        Screen.Stats -> if (isEnglish) "Stats" else "Statistik"
                                        Screen.Restaurants -> if (isEnglish) "Restaurants" else "Restauranger"
                                        Screen.Settings -> if (isEnglish) "Settings" else "Inställningar"
                                    },
                                    style = TextStyle(fontSize = 7.2.sp),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = navTextColor,
                                selectedTextColor = navTextColor,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                unselectedIconColor = navTextColor.copy(alpha = 0.78f),
                                unselectedTextColor = navTextColor.copy(alpha = 0.78f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (items[page]) {
                    Screen.Home -> HomeScreen(
                        uiState = uiState,
                        transactions = transactions,
                        presets = presets,
                        restaurants = restaurants,
                        onAddTransaction = viewModel::addTransaction,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onAddPreset = viewModel::addPreset,
                        onDeletePreset = viewModel::deletePreset,
                        onNavigateToWeeklySummary = {},
                        onNavigateToSettings = { scope.launch { pagerState.animateScrollToPage(items.indexOf(Screen.Settings)) } },
                        isHapticEnabled = isHapticEnabled,
                        currentTheme = currentTheme,
                        language = language
                    )

                    Screen.History -> HistoryScreen(
                        transactions = transactions,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onClearAllTransactions = viewModel::clearAllTransactions,
                        isHapticEnabled = isHapticEnabled,
                        language = language
                    )

                    Screen.Stats -> StatsScreen(
                        uiState = uiState,
                        transactions = transactions
                    )

                    Screen.Restaurants -> RestaurantsScreen(
                        restaurants = restaurants,
                        onAddRestaurant = viewModel::addRestaurant,
                        onDeleteRestaurant = viewModel::deleteRestaurant,
                        isHapticEnabled = isHapticEnabled,
                        language = language
                    )

                    Screen.Settings -> SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateBack = {},
                        periodBudgetRemaining = uiState.periodBudgetRemaining,
                        onSetPeriodBudget = viewModel::setPeriodBudgetRemaining,
                        onNavigateToHolidays = {},
                        isHapticEnabled = isHapticEnabled,
                        onToggleHaptic = viewModel::toggleHaptic,
                        currentTheme = currentTheme,
                        onSetTheme = onSetTheme,
                        setupPreferences = setupPreferences,
                        holidays = holidays,
                        onResetBalance = viewModel::resetBalance,
                        onSetManualBalance = viewModel::setManualBalance,
                        onSetPeriodDates = viewModel::setPeriodDates,
                        onSetDailyIncome = viewModel::setDailyIncome,
                        language = language,
                        onSetLanguage = viewModel::setLanguage,
                        reminderMinutes = reminderMinutes,
                        onSetReminderMinutes = viewModel::setReminderMinutes,
                        notificationsEnabled = notificationsEnabled,
                        onSetNotificationsEnabled = viewModel::setNotificationsEnabled,
                        autoHolidaysEnabled = autoHolidaysEnabled,
                        onSetAutoHolidaysEnabled = viewModel::setAutoHolidaysEnabled,
                        onAddHoliday = viewModel::addHoliday,
                        onDeleteHoliday = viewModel::deleteHoliday
                    )
                }
            }
        }
    }
}
