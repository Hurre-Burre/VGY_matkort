package com.example.vgy_matkort.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ImportedHoliday(
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

object HolidayImporter {
    private const val VGY_URL = "https://vgy.se/lasarsdata/"
    
    /**
     * Fetches and parses holiday data from Värmdö Gymnasium's website
     * Returns only major holidays: Höstlov, Jullov, Sportlov, Påsklov
     */
    suspend fun fetchHolidaysFromVGY(): Result<List<ImportedHoliday>> = withContext(Dispatchers.IO) {
        try {
            val htmlContent = fetchWebPage(VGY_URL)
            val holidays = parseHolidays(htmlContent)
            Result.success(holidays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun fetchWebPage(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        return try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            reader.use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
    
    private fun parseHolidays(htmlContent: String): List<ImportedHoliday> {
        val holidays = mutableListOf<ImportedHoliday>()
        
        // Major holidays we want to import
        val majorHolidays = listOf("Höstlov", "Jullov", "Sportlov", "Påsklov")
        
        // Patterns to match:
        // "27/10 – 31/10 Höstlov"
        // "22/12 – 7/1 Jullov"
        // "23/2 – 27/2 Sportlov"
        // "3/4 – 10/4 Påsklov"
        
        val dateRangePattern = Regex("""(\d{1,2})/(\d{1,2})\s*[–-]\s*(\d{1,2})/(\d{1,2})\s+(\w+)""")
        
        dateRangePattern.findAll(htmlContent).forEach { matchResult ->
            val (startDay, startMonth, endDay, endMonth, name) = matchResult.destructured
            
            // Check if this is a major holiday
            if (majorHolidays.any { name.contains(it, ignoreCase = true) }) {
                try {
                    // Determine the year based on the month
                    val currentYear = LocalDate.now().year
                    val currentMonth = LocalDate.now().monthValue
                    
                    // If we're in autumn/winter and the holiday is in spring, it's next year
                    // If we're in spring and the holiday is in autumn/winter, it's this year or next
                    var startYear = currentYear
                    var endYear = currentYear
                    
                    val startMonthInt = startMonth.toInt()
                    val endMonthInt = endMonth.toInt()
                    
                    // Handle year transitions (e.g., Jullov from Dec to Jan)
                    if (startMonthInt > endMonthInt) {
                        endYear = startYear + 1
                    }
                    
                    // If we're past the holiday this year, use next year
                    if (currentMonth > endMonthInt || (currentMonth == endMonthInt && LocalDate.now().dayOfMonth > endDay.toInt())) {
                        startYear++
                        endYear++
                    }
                    
                    val startDate = LocalDate.of(startYear, startMonthInt, startDay.toInt())
                    val endDate = LocalDate.of(endYear, endMonthInt, endDay.toInt())
                    
                    // Clean up the name
                    val cleanName = when {
                        name.contains("Höstlov", ignoreCase = true) -> "Höstlov"
                        name.contains("Jullov", ignoreCase = true) -> "Jullov"
                        name.contains("Sportlov", ignoreCase = true) -> "Sportlov"
                        name.contains("Påsklov", ignoreCase = true) -> "Påsklov"
                        else -> name
                    }
                    
                    holidays.add(ImportedHoliday(cleanName, startDate, endDate))
                } catch (e: Exception) {
                    // Skip invalid dates
                }
            }
        }
        
        return holidays
    }
    
    /**
     * Parses holidays from CSV format
     * Expected format: name,startDate,endDate
     * Date format: YYYY-MM-DD
     */
    fun parseFromCSV(csvContent: String): List<ImportedHoliday> {
        val holidays = mutableListOf<ImportedHoliday>()
        val lines = csvContent.lines().filter { it.isNotBlank() }
        
        lines.forEach { line ->
            val parts = line.split(",").map { it.trim() }
            if (parts.size >= 3) {
                try {
                    val name = parts[0]
                    val startDate = LocalDate.parse(parts[1])
                    val endDate = LocalDate.parse(parts[2])
                    holidays.add(ImportedHoliday(name, startDate, endDate))
                } catch (e: Exception) {
                    // Skip invalid lines
                }
            }
        }
        
        return holidays
    }
}
