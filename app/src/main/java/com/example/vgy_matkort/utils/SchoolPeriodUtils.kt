package com.example.vgy_matkort.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

data class SchoolPeriod(
    val name: String,
    val start: LocalDate,
    val end: LocalDate
)

object SchoolPeriodUtils {
    // Auto-holidays for Stockholm pattern: höstlov v44, sportlov v9, påsklov veckan efter påskdagen,
    // jullov, sommarlov. Generated per year window.
    val defaultHolidays: List<Pair<ClosedRange<LocalDate>, String>>
        get() {
            val currentYear = LocalDate.now().year
            return (currentYear - 1..currentYear + 2)
                .flatMap { generateStockholmSchoolHolidays(it) }
                .distinctBy { Triple(it.first.start, it.first.endInclusive, it.second) }
        }

    private fun generateStockholmSchoolHolidays(year: Int): List<Pair<ClosedRange<LocalDate>, String>> {
        val sportlovStart = mondayOfIsoWeek(year, 9)
        val hostlovStart = mondayOfIsoWeek(year, 44)
        val easterSunday = easterSunday(year)
        val pasklovStart = easterSunday.plusDays(1)

        val jullovStart = LocalDate.of(year, 12, 23)
        val jullovEnd = LocalDate.of(year + 1, 1, 7)

        val sommarlovStart = LocalDate.of(year, 6, 12)
        val sommarlovEnd = LocalDate.of(year, 8, 18)

        return listOf(
            (sportlovStart..sportlovStart.plusDays(4)) to "Sportlov",
            (pasklovStart..pasklovStart.plusDays(4)) to "Påsklov",
            (sommarlovStart..sommarlovEnd) to "Sommarlov",
            (hostlovStart..hostlovStart.plusDays(4)) to "Höstlov",
            (jullovStart..jullovEnd) to "Jullov"
        )
    }

    private fun mondayOfIsoWeek(year: Int, week: Int): LocalDate {
        return LocalDate.of(year, 1, 4)
            .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week.toLong())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    // Gregorian algorithm (Meeus/Jones/Butcher).
    private fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

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

    fun getAccumulatedBudget(period: SchoolPeriod, holidays: List<ClosedRange<LocalDate>>, currentDate: LocalDate = LocalDate.now(), dailyIncome: Int = 70): Int {
        return getDaysPassedInPeriod(period, holidays, currentDate) * dailyIncome
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

    fun getDailyAvailable(currentBalance: Int, period: SchoolPeriod, holidays: List<ClosedRange<LocalDate>>, currentDate: LocalDate = LocalDate.now(), dailyIncome: Int = 70): Int {
        val remainingDays = getRemainingSchoolDays(period, holidays, currentDate)
        if (remainingDays == 0) return 0
        
        // Future income is days *after* today * dailyIncome
        val futureDays = getRemainingSchoolDays(period, holidays, currentDate.plusDays(1))
        val futureIncome = futureDays * dailyIncome
        
        val totalAvailable = currentBalance + futureIncome
        return totalAvailable / remainingDays
    }
}
