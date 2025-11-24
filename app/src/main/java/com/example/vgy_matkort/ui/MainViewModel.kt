package com.example.vgy_matkort.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vgy_matkort.data.AppDatabase
import com.example.vgy_matkort.data.Preset
import com.example.vgy_matkort.data.Transaction
import com.example.vgy_matkort.utils.SchoolPeriodUtils
import com.example.vgy_matkort.utils.HolidayImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val presetDao = database.presetDao()
    private val holidayDao = database.holidayDao()

    val transactions: StateFlow<List<Transaction>> = transactionDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presets: StateFlow<List<Preset>> = presetDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val holidays: StateFlow<List<com.example.vgy_matkort.data.Holiday>> = holidayDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentDate = MutableStateFlow(LocalDate.now())
    
    private val sharedPreferences = application.getSharedPreferences("vgy_matkort_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow(sharedPreferences.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    private val _shouldShowTutorial = MutableStateFlow(!sharedPreferences.getBoolean("has_seen_tutorial", false))
    val shouldShowTutorial: StateFlow<Boolean> = _shouldShowTutorial.asStateFlow()

    private val _tutorialStep = MutableStateFlow(0)
    val tutorialStep: StateFlow<Int> = _tutorialStep.asStateFlow()

    private val _highlightRegistry = MutableStateFlow<Map<String, androidx.compose.ui.geometry.Rect>>(emptyMap())
    val highlightRegistry: StateFlow<Map<String, androidx.compose.ui.geometry.Rect>> = _highlightRegistry.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(sharedPreferences.getBoolean("is_haptic_enabled", true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _currentTheme = MutableStateFlow(
        try {
            com.example.vgy_matkort.ui.theme.AppTheme.valueOf(sharedPreferences.getString("app_theme", "Blue") ?: "Blue")
        } catch (e: Exception) {
            com.example.vgy_matkort.ui.theme.AppTheme.Blue
        }
    )
    val currentTheme: StateFlow<com.example.vgy_matkort.ui.theme.AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: com.example.vgy_matkort.ui.theme.AppTheme) {
        _currentTheme.value = theme
        sharedPreferences.edit().putString("app_theme", theme.name).apply()
    }

    
    init {
        viewModelScope.launch {
            val existingHolidays = holidayDao.getAll().first()
            
            if (existingHolidays.isEmpty()) {
                // Initial migration with names
                SchoolPeriodUtils.defaultHolidays.forEach { (range, name) ->
                    val start = range.start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = range.endInclusive.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    holidayDao.insert(com.example.vgy_matkort.data.Holiday(startDate = start, endDate = end, name = name))
                }
            } else {
                // Rename generic "Holiday" entries if they match default dates
                existingHolidays.filter { it.name == "Holiday" }.forEach { dbHoliday ->
                    val dbStart = java.time.Instant.ofEpochMilli(dbHoliday.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    val dbEnd = java.time.Instant.ofEpochMilli(dbHoliday.endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    
                    val match = SchoolPeriodUtils.defaultHolidays.find { (range, _) ->
                        range.start == dbStart && range.endInclusive == dbEnd
                    }
                    
                    if (match != null) {
                        // Update name
                        holidayDao.delete(dbHoliday) // Delete old
                        holidayDao.insert(dbHoliday.copy(name = match.second)) // Insert new with name
                    }
                }
                
                // Ensure "Jullov" exists for the current year
                val jullov = SchoolPeriodUtils.defaultHolidays.find { it.second == "Jullov" }
                if (jullov != null) {
                    val (range, name) = jullov
                    val start = range.start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = range.endInclusive.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    
                    val exists = existingHolidays.any { 
                        val dbStart = java.time.Instant.ofEpochMilli(it.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        dbStart == range.start
                    }
                    
                    if (!exists) {
                        holidayDao.insert(com.example.vgy_matkort.data.Holiday(startDate = start, endDate = end, name = name))
                    }
                }
            }
        }
    }
    
    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun toggleHaptic(isEnabled: Boolean) {
        _isHapticEnabled.value = isEnabled
        sharedPreferences.edit().putBoolean("is_haptic_enabled", isEnabled).apply()
    }
    
    fun markTutorialAsSeen() {
        _shouldShowTutorial.value = false
        sharedPreferences.edit().putBoolean("has_seen_tutorial", true).apply()
    }
    
    fun showTutorial() {
        _tutorialStep.value = 0
        _shouldShowTutorial.value = true
    }

    fun nextTutorialStep() {
        _tutorialStep.value++
    }

    fun prevTutorialStep() {
        if (_tutorialStep.value > 0) {
            _tutorialStep.value--
        }
    }
    
    fun setTutorialStep(step: Int) {
        _tutorialStep.value = step
    }

    fun registerHighlight(key: String, rect: androidx.compose.ui.geometry.Rect) {
        val current = _highlightRegistry.value.toMutableMap()
        current[key] = rect
        _highlightRegistry.value = current
    }

    
    val uiState = combine(transactions, _currentDate, holidays) { transactions, date, dbHolidays ->
        // Convert DB holidays to ranges first
        val holidayRanges = dbHolidays.map { 
            val start = java.time.Instant.ofEpochMilli(it.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            val end = java.time.Instant.ofEpochMilli(it.endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            start..end
        }

        // Get dynamic period based on date and holidays
        val period = SchoolPeriodUtils.getCurrentPeriod(date, holidayRanges)
        
        // Find the last holiday that has ended before today
        val lastHolidayEnd = holidayRanges
            .filter { it.endInclusive.isBefore(date) }
            .maxOfOrNull { it.endInclusive }
        
        // Calculate from the day after the last holiday, or period start if no holidays have ended
        val calculationStartDate = lastHolidayEnd?.plusDays(1) ?: period?.start
        
        // Calculate accumulated budget from calculationStartDate to today (not from period start)
        val accumulatedBudget = if (period != null && calculationStartDate != null) {
            var budget = 0
            var d = calculationStartDate!!
            while (!d.isAfter(date) && !d.isAfter(period.end)) {
                if (SchoolPeriodUtils.isSchoolDay(d, holidayRanges)) {
                    budget += 70
                }
                d = d.plusDays(1)
            }
            budget
        } else {
            0
        }
        
        // Filter transactions that happened after the last holiday (or period start)
        val periodTransactions = if (period != null && calculationStartDate != null) {
            transactions.filter { 
                val txDate = java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                !txDate.isBefore(calculationStartDate!!) && !txDate.isAfter(period.end)
            }
        } else {
            emptyList()
        }
        
        val spent = periodTransactions.sumOf { it.amount }
        val currentBalance = accumulatedBudget - spent
        
        // Weekly Summaries
        // Group transactions by week and calculate balance at end of each week
        // Start from the end of the last completed holiday, with balance starting at 0
        val weeklySummaries = if (period != null) {
            val summaries = mutableListOf<WeekSummary>()
            
            // Find the last holiday that has ended before today
            val lastHolidayEnd = holidayRanges
                .filter { it.endInclusive.isBefore(date) }
                .maxOfOrNull { it.endInclusive }
            
            // Start from the day after the last holiday, or period start if no holidays have ended
            val startDate = lastHolidayEnd?.plusDays(1) ?: period.start
            
            var weekDate = startDate
            // Find the first Sunday (or end of week)
            while (weekDate.dayOfWeek != java.time.DayOfWeek.SUNDAY && !weekDate.isAfter(period.end)) {
                weekDate = weekDate.plusDays(1)
            }
            
            while (!weekDate.isAfter(date) && !weekDate.isAfter(period.end)) {
                // Calculate accumulated budget from startDate to weekDate (not from period start)
                var accBudgetSinceStart = 0
                var d = startDate
                while (!d.isAfter(weekDate)) {
                    if (SchoolPeriodUtils.isSchoolDay(d, holidayRanges)) {
                        accBudgetSinceStart += 70
                    }
                    d = d.plusDays(1)
                }
                
                // Calculate spent from startDate to weekDate
                val spentSinceStart = periodTransactions
                    .filter { 
                        val txDate = java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        !txDate.isBefore(startDate) && !txDate.isAfter(weekDate)
                    }
                    .sumOf { it.amount }
                
                val balanceAtWeek = accBudgetSinceStart - spentSinceStart
                val weekNum = weekDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                
                summaries.add(WeekSummary(weekNum, balanceAtWeek))
                
                // Move to next week
                weekDate = weekDate.plusWeeks(1)
            }
            summaries.reversed() // Show newest first
        } else {
            emptyList()
        }
        
        // Current Week Stats (Isolated)
        var currentWeekBalance = 0
        var currentWeekSpent = 0
        var currentWeekAccumulated = 0
        
        if (period != null && calculationStartDate != null) {
            val today = date
            // Find start of current week (Monday)
            var startOfWeek = today
            while (startOfWeek.dayOfWeek != java.time.DayOfWeek.MONDAY) {
                startOfWeek = startOfWeek.minusDays(1)
            }
            // Cap at calculationStartDate (don't go before last holiday)
            if (startOfWeek.isBefore(calculationStartDate!!)) {
                startOfWeek = calculationStartDate!!
            }
            
            // Calculate accumulated budget just for this week (from startOfWeek to Today)
            var daysInWeek = 0
            var d = startOfWeek
            while (!d.isAfter(today)) {
                if (SchoolPeriodUtils.isSchoolDay(d, holidayRanges)) {
                     daysInWeek++
                }
                d = d.plusDays(1)
            }
            currentWeekAccumulated = daysInWeek * 70
            
            // Calculate spent just for this week
            currentWeekSpent = transactions.filter { 
                val txDate = java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                !txDate.isBefore(startOfWeek) && !txDate.isAfter(today)
            }.sumOf { it.amount }
            
            currentWeekBalance = currentWeekAccumulated - currentWeekSpent
        }
        
        
        // Chart Data Calculation
        val chartData = if (period != null && calculationStartDate != null) {
            val points = mutableListOf<ChartPoint>()
            var chartDate = calculationStartDate!!
            val endDate = if (date.isBefore(period.end)) date else period.end
            var dayIndex = 0
            
            // Optimization: Filter transactions once (from calculationStartDate)
            val relevantTransactions = transactions.filter { 
                val txDate = java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                !txDate.isBefore(calculationStartDate!!) && !txDate.isAfter(endDate)
            }

            while (!chartDate.isAfter(endDate)) {
                // Accumulated budget from calculationStartDate to this day
                var accBudget = 0
                var d = calculationStartDate!!
                while (!d.isAfter(chartDate)) {
                    if (SchoolPeriodUtils.isSchoolDay(d, holidayRanges)) {
                        accBudget += 70
                    }
                    d = d.plusDays(1)
                }
                
                // Spent from calculationStartDate to this day
                val spentOnDay = relevantTransactions
                    .filter { 
                        val txDate = java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        !txDate.isAfter(chartDate)
                    }
                    .sumOf { it.amount }
                
                val balance = accBudget - spentOnDay
                points.add(ChartPoint(dayIndex++, balance, chartDate))
                
                chartDate = chartDate.plusDays(1)
            }
            points
        } else {
            emptyList()
        }
        
        
        // Calculate remaining budget for the entire period (from calculationStartDate to period end)
        val totalPeriodBudget = if (period != null && calculationStartDate != null) {
            var budget = 0
            var d = calculationStartDate!!
            while (!d.isAfter(period.end)) {
                if (SchoolPeriodUtils.isSchoolDay(d, holidayRanges)) {
                    budget += 70
                }
                d = d.plusDays(1)
            }
            budget
        } else {
            0
        }
        
        val periodBudgetRemaining = totalPeriodBudget - spent
        
        UiState(
            currentBalance = currentBalance,
            initialBalance = accumulatedBudget,
            periodName = period?.name ?: "No Active Period",
            periodEnd = period?.end,
            weeklySummaries = weeklySummaries,
            totalPeriodBudget = totalPeriodBudget,
            periodBudgetRemaining = periodBudgetRemaining,
            daysRemaining = period?.let { SchoolPeriodUtils.getRemainingSchoolDays(it, holidayRanges, date) } ?: 0,
            dailyAvailable = period?.let { SchoolPeriodUtils.getDailyAvailable(currentBalance, it, holidayRanges, date) } ?: 0,
            chartData = chartData,
            currentWeekBalance = currentWeekBalance,
            currentWeekSpent = currentWeekSpent,
            currentWeekAccumulated = currentWeekAccumulated
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun addTransaction(amount: Int) {
        viewModelScope.launch {
            transactionDao.insert(Transaction(amount = amount, timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
        }
    }

    fun addPreset(amount: Int, label: String) {
        viewModelScope.launch {
            presetDao.insert(Preset(amount = amount, label = label))
        }
    }
    
    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            presetDao.delete(preset)
        }
    }
    
    fun addHoliday(startDate: Long, endDate: Long, name: String) {
        viewModelScope.launch {
            holidayDao.insert(com.example.vgy_matkort.data.Holiday(startDate = startDate, endDate = endDate, name = name))
        }
    }
    
    fun deleteHoliday(holiday: com.example.vgy_matkort.data.Holiday) {
        viewModelScope.launch {
            holidayDao.delete(holiday)
        }
    }
    
    fun resetBalance() {
        viewModelScope.launch {
            val currentBalance = uiState.value.currentBalance
            if (currentBalance != 0) {
                // To reset to 0, we need to subtract the current balance.
                // If balance is 100, we add transaction of 100 (spent).
                // If balance is -50, we add transaction of -50 (refund/correction).
                transactionDao.insert(Transaction(amount = currentBalance, timestamp = System.currentTimeMillis()))
            }
        }
    }
    
    fun setManualBalance(targetBalance: Int) {
        viewModelScope.launch {
            val currentBalance = uiState.value.currentBalance
            val correction = currentBalance - targetBalance
            // If current is 100 and target is 150, correction = -50 (add money)
            // If current is 100 and target is 50, correction = 50 (subtract money)
            if (correction != 0) {
                transactionDao.insert(Transaction(amount = correction, timestamp = System.currentTimeMillis()))
            }
        }
    }

    fun setPeriodBudgetRemaining(targetBalance: Int) {
        viewModelScope.launch {
            val currentRemaining = uiState.value.periodBudgetRemaining
            val correction = currentRemaining - targetBalance
            // If remaining is 1000 and target is 800, correction = 200 (we spent 200 more)
            // If remaining is 1000 and target is 1200, correction = -200 (we spent 200 less)
            if (correction != 0) {
                transactionDao.insert(Transaction(amount = correction, timestamp = System.currentTimeMillis()))
            }
        }
    }
    
    suspend fun importHolidaysFromWeb(): Result<Int> {
        return try {
            val result = HolidayImporter.fetchHolidaysFromVGY()
            result.fold(
                onSuccess = { importedHolidays ->
                    var importedCount = 0
                    importedHolidays.forEach { imported ->
                        val start = imported.startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val end = imported.endDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        
                        // Check if holiday already exists
                        val exists = holidays.value.any { existing ->
                            val existingStart = java.time.Instant.ofEpochMilli(existing.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            existingStart == imported.startDate
                        }
                        
                        if (!exists) {
                            holidayDao.insert(com.example.vgy_matkort.data.Holiday(
                                startDate = start,
                                endDate = end,
                                name = imported.name
                            ))
                            importedCount++
                        }
                    }
                    Result.success(importedCount)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class UiState(
    val currentBalance: Int = 0,
    val initialBalance: Int = 0,
    val periodName: String = "",
    val periodEnd: LocalDate? = null,
    val weeklySummaries: List<WeekSummary> = emptyList(),
    val totalPeriodBudget: Int = 0,
    val periodBudgetRemaining: Int = 0,
    val daysRemaining: Int = 0,
    val dailyAvailable: Int = 0,
    val chartData: List<ChartPoint> = emptyList(),
    val currentWeekBalance: Int = 0,
    val currentWeekSpent: Int = 0,
    val currentWeekAccumulated: Int = 0
)

data class WeekSummary(
    val weekNumber: Int,
    val balance: Int
)

data class ChartPoint(
    val dayIndex: Int,
    val balance: Int,
    val date: LocalDate
)
