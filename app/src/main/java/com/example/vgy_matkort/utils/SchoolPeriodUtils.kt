package com.example.vgy_matkort.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SchoolPeriod(
    val name: String,
    val start: LocalDate,
    val end: LocalDate
)

object SchoolPeriodUtils {
    // Default holidays for initial migration (keep these as they are useful defaults)
    val defaultHolidays = listOf(
        LocalDate.of(2024, 10, 28)..LocalDate.of(2024, 11, 1) to "Höstlov v.44",
        LocalDate.of(2024, 12, 23)..LocalDate.of(2025, 1, 7) to "Jullov", // Added explicitly as a holiday
        LocalDate.of(2025, 2, 17)..LocalDate.of(2025, 2, 21) to "Sportlov v.8",
        LocalDate.of(2025, 4, 14)..LocalDate.of(2025, 4, 17) to "Påsklov",
        
        // Autumn 2025
        LocalDate.of(2025, 10, 27)..LocalDate.of(2025, 10, 31) to "Höstlov v.44",
        LocalDate.of(2025, 12, 23)..LocalDate.of(2026, 1, 7) to "Jullov",
        
        // Spring 2026
        LocalDate.of(2026, 2, 23)..LocalDate.of(2026, 2, 27) to "Sportlov v.9",
        LocalDate.of(2026, 4, 3)..LocalDate.of(2026, 4, 10) to "Påsklov",
        LocalDate.of(2026, 5, 14)..LocalDate.of(2026, 5, 15) to "Lov (Kristi Himmelsfärd)"
    )

    fun getCurrentPeriod(date: LocalDate = LocalDate.now(), holidays: List<ClosedRange<LocalDate>> = emptyList()): SchoolPeriod? {
        val semesterStart = getSemesterStart(date)
        val semesterEnd = getSemesterEnd(date)
        
        // Find the next holiday that starts after the current date (or after semester start if we are before it)
        // We only care about holidays that are within the current semester
        val searchStartDate = if (date.isBefore(semesterStart)) semesterStart else date
        
        val nextHoliday = holidays
            .filter { !it.start.isBefore(searchStartDate) && !it.start.isAfter(semesterEnd) }
            .minByOrNull { it.start }
            
        // The period ends the day before the next holiday starts, OR at the end of the semester if no holiday is found
        val periodEnd = nextHoliday?.start?.minusDays(1) ?: semesterEnd
        
        // Name based on target
        val periodName = if (nextHoliday != null) {
            "Until Holiday (${periodEnd.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${periodEnd.dayOfMonth})"
        } else {
            "Until Semester End"
        }
        
        return SchoolPeriod(periodName, semesterStart, periodEnd)
    }
    
    private fun getSemesterStart(date: LocalDate): LocalDate {
        val year = date.year
        // If we are in the second half of the year (July-Dec), it's Autumn semester
        return if (date.monthValue >= 7) {
            LocalDate.of(year, 8, 19) // Autumn starts ~Aug 19
        } else {
            // Spring semester
            LocalDate.of(year, 1, 8) // Spring starts ~Jan 8
        }
    }
    
    private fun getSemesterEnd(date: LocalDate): LocalDate {
        val year = date.year
        return if (date.monthValue >= 7) {
            LocalDate.of(year, 12, 22) // Autumn ends ~Dec 22 (default fallback)
        } else {
            LocalDate.of(year, 6, 12) // Spring ends ~June 12
        }
    }

    fun getSchoolDaysInPeriod(period: SchoolPeriod, holidays: List<ClosedRange<LocalDate>>): Int {
        var days = 0
        var currentDate = period.start
        while (!currentDate.isAfter(period.end)) {
            if (isSchoolDay(currentDate, holidays)) {
                days++
            }
            currentDate = currentDate.plusDays(1)
        }
        return days
    }
    
    fun isSchoolDay(date: LocalDate, holidays: List<ClosedRange<LocalDate>>): Boolean {
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) return false
        
        // Check if it's in a holiday range
        if (holidays.any { range -> !date.isBefore(range.start) && !date.isAfter(range.endInclusive) }) {
            return false
        }
        
        return true
    }

    fun getDaysPassedInPeriod(period: SchoolPeriod, holidays: List<ClosedRange<LocalDate>>, currentDate: LocalDate = LocalDate.now()): Int {
        if (currentDate.isBefore(period.start)) return 0
        
        var days = 0
        var date = period.start
        // Count up to AND INCLUDING today
        val endDate = if (currentDate.isAfter(period.end)) period.end else currentDate
        
        while (!date.isAfter(endDate)) {
            if (isSchoolDay(date, holidays)) {
                days++
            }
            date = date.plusDays(1)
        }
        return days
    }

    fun getAccumulatedBudget(period: SchoolPeriod, holidays: List<ClosedRange<LocalDate>>, currentDate: LocalDate = LocalDate.now()): Int {
        return getDaysPassedInPeriod(period, holidays, currentDate) * 70
    }

    fun getRemainingSchoolDays(period: SchoolPeriod, holidays: List<ClosedRange<LocalDate>>, currentDate: LocalDate = LocalDate.now()): Int {
        if (currentDate.isAfter(period.end)) return 0
        
        var days = 0
        // Start counting from today if it's within the period, otherwise from start of period
        var date = if (currentDate.isBefore(period.start)) period.start else currentDate
        
        while (!date.isAfter(period.end)) {
            if (isSchoolDay(date, holidays)) {
                days++
            }
            date = date.plusDays(1)
        }
        return days
    }

    fun getDailyAvailable(currentBalance: Int, period: SchoolPeriod, holidays: List<ClosedRange<LocalDate>>, currentDate: LocalDate = LocalDate.now()): Int {
        val remainingDays = getRemainingSchoolDays(period, holidays, currentDate)
        if (remainingDays == 0) return 0
        
        // Future income is days *after* today * 70
        val futureDays = getRemainingSchoolDays(period, holidays, currentDate.plusDays(1))
        val futureIncome = futureDays * 70
        
        val totalAvailable = currentBalance + futureIncome
        return totalAvailable / remainingDays
    }
}
