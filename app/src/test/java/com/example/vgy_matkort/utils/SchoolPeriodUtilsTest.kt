package com.example.vgy_matkort.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SchoolPeriodUtilsTest {

    @Test
    fun testHolidayReducesBudget() {
        // Setup
        val period = SchoolPeriod("Test Period", LocalDate.of(2025, 8, 19), LocalDate.of(2025, 12, 19))
        val currentDate = LocalDate.of(2025, 11, 19) // Wednesday
        
        // No holidays
        val holidaysEmpty = emptyList<ClosedRange<LocalDate>>()
        val budgetBefore = SchoolPeriodUtils.getAccumulatedBudget(period, holidaysEmpty, currentDate)
        
        // Add holiday for yesterday (Tuesday, Nov 18)
        val holidayRange = LocalDate.of(2025, 11, 18)..LocalDate.of(2025, 11, 18)
        val holidaysWithOne = listOf(holidayRange)
        
        val budgetAfter = SchoolPeriodUtils.getAccumulatedBudget(period, holidaysWithOne, currentDate)
        
        // Verify
        println("Budget Before: $budgetBefore")
        println("Budget After: $budgetAfter")
        
        // Difference should be 70kr
        assertEquals("Budget should decrease by 70kr", budgetBefore - 70, budgetAfter)
    }
}
