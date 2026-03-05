package com.example.vgy_matkort.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vgy_matkort.data.Holiday
import com.example.vgy_matkort.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    periodBudgetRemaining: Int = 0,
    onSetPeriodBudget: (Int) -> Unit,
    onNavigateToHolidays: () -> Unit = {},
    isHapticEnabled: Boolean,
    onToggleHaptic: (Boolean) -> Unit,
    currentTheme: AppTheme,
    onSetTheme: (AppTheme) -> Unit,
    setupPreferences: SetupPreferences,
    holidays: List<Holiday>,
    onResetBalance: () -> Unit,
    onSetManualBalance: (Int) -> Unit,
    onSetPeriodDates: (LocalDate, LocalDate) -> Unit,
    onSetDailyIncome: (Int) -> Unit,
    language: String,
    onSetLanguage: (String) -> Unit,
    reminderMinutes: Int,
    onSetReminderMinutes: (Int) -> Unit,
    notificationsEnabled: Boolean,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    autoHolidaysEnabled: Boolean,
    onSetAutoHolidaysEnabled: (Boolean) -> Unit,
    onAddHoliday: (Long, Long, String) -> Unit,
    onDeleteHoliday: (Holiday) -> Unit
) {
    val context = LocalContext.current
    val isEnglish = language == "en"

    var customBalanceText by remember { mutableStateOf(periodBudgetRemaining.toString()) }
    var dailyBudgetText by remember { mutableStateOf(setupPreferences.dailyIncome.toString()) }
    var showBalanceDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var startDate by remember(setupPreferences.periodStart) {
        mutableStateOf(
            Instant.ofEpochMilli(setupPreferences.periodStart)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        )
    }
    var endDate by remember(setupPreferences.periodEnd) {
        mutableStateOf(
            Instant.ofEpochMilli(setupPreferences.periodEnd)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        )
    }

    var holidayName by remember { mutableStateOf("") }
    var holidayStart by remember { mutableStateOf(LocalDate.now()) }
    var holidayEnd by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var showHolidayStartPicker by remember { mutableStateOf(false) }
    var showHolidayEndPicker by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val currentLocale = if (isEnglish) Locale.forLanguageTag("en-US") else Locale.forLanguageTag("sv-SE")
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", currentLocale)
    val holidayDateFormatter = DateTimeFormatter.ofPattern("d MMM", currentLocale)
    val cardColor = if (currentTheme == AppTheme.Signature || (currentTheme == AppTheme.System && !isDarkTheme)) {
        Color(0xCCFFFFFF)
    } else {
        Color(0x26FFFFFF)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = if (isEnglish) "Settings" else "Inställningar",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SectionTitle(if (isEnglish) "Balance" else "Saldo")
        ValueRow(if (isEnglish) "Custom balance" else "Anpassat saldo", periodBudgetRemaining.toString()) {
            customBalanceText = periodBudgetRemaining.toString()
            showBalanceDialog = true
        }
        Spacer(modifier = Modifier.height(8.dp))
        ValueRow(if (isEnglish) "Set period budget to 0" else "Sätt periodbudget till 0", if (isEnglish) "Reset" else "Nollställ") { onSetPeriodBudget(0) }
        Spacer(modifier = Modifier.height(8.dp))
        ValueRow(if (isEnglish) "Reset balance to 0" else "Återställ saldo till 0", if (isEnglish) "Reset" else "Återställ") { showResetDialog = true }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(if (isEnglish) "Budget" else "Budget")
        ValueRow(if (isEnglish) "Daily budget" else "Daglig budget", dailyBudgetText) {
            dailyBudgetText.toIntOrNull()?.let(onSetDailyIncome)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(if (isEnglish) "Period" else "Period")
        DateRow(if (isEnglish) "Start date" else "Startdatum", startDate.format(dateFormatter)) { showStartPicker = true }
        Spacer(modifier = Modifier.height(10.dp))
        DateRow(if (isEnglish) "End date" else "Slutdatum", endDate.format(dateFormatter)) { showEndPicker = true }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(if (isEnglish) "Holiday handling" else "Lovhantering")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEnglish) "Automatic holidays" else "Automatiska lov", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Switch(
                        checked = autoHolidaysEnabled,
                        onCheckedChange = onSetAutoHolidaysEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (autoHolidaysEnabled) {
                    Text(
                        text = if (isEnglish) "Common school breaks for Stockholm are used automatically." else "Vanliga skollov för Stockholm används automatiskt.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        if (!autoHolidaysEnabled) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = holidayName,
                        onValueChange = { holidayName = it },
                        label = { Text(if (isEnglish) "Holiday name" else "Lovnamn") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DateRow(if (isEnglish) "Start date" else "Startdatum", holidayStart.format(holidayDateFormatter)) {
                        showHolidayStartPicker = true
                    }
                    DateRow(if (isEnglish) "End date" else "Slutdatum", holidayEnd.format(holidayDateFormatter)) {
                        showHolidayEndPicker = true
                    }

                    Button(
                        onClick = {
                            if (holidayName.isNotBlank()) {
                                val startMillis = holidayStart
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                val endMillis = holidayEnd
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                onAddHoliday(startMillis, endMillis, holidayName.trim())
                                holidayName = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isEnglish) "Add holiday" else "Lägg till lov", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            holidays.forEach { holiday ->
                SwipeRevealHolidayRow(
                    holiday = holiday,
                    holidayDateFormatter = holidayDateFormatter,
                    cardColor = cardColor,
                    onDeleteHoliday = onDeleteHoliday
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(if (isEnglish) "Reminder time" else "Påminnelsetid")
        ToggleRow(if (isEnglish) "Notifications" else "Notiser", notificationsEnabled, onSetNotificationsEnabled)
        Spacer(modifier = Modifier.height(8.dp))
        ValueRow(
            if (isEnglish) "Reminder time" else "Påminnelsetid",
            "%02d:%02d".format(reminderMinutes / 60, reminderMinutes % 60)
        ) {
            if (notificationsEnabled) {
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onSetReminderMinutes(hour * 60 + minute) },
                    reminderMinutes / 60,
                    reminderMinutes % 60,
                    true
                ).show()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(if (isEnglish) "Language" else "Språk")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LangChip("Svenska", language == "sv") { onSetLanguage("sv") }
                LangChip("English", language == "en") { onSetLanguage("en") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(if (isEnglish) "Appearance" else "Utseende")
        ValueRow(if (isEnglish) "Theme" else "Tema", currentTheme.name) { showThemeDialog = true }
        Spacer(modifier = Modifier.height(8.dp))
        ToggleRow(if (isEnglish) "Dark mode" else "Mörkt tema", isDarkTheme, onToggleTheme)
        Spacer(modifier = Modifier.height(10.dp))
        ToggleRow(if (isEnglish) "Haptic feedback" else "Haptisk feedback", isHapticEnabled, onToggleHaptic)

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Support")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@matkort.se")
                        putExtra(Intent.EXTRA_SUBJECT, "Matkort support")
                    }
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.MailOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(if (isEnglish) "Contact support" else "Kontakta support", color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    if (showBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showBalanceDialog = false },
            title = { Text(if (isEnglish) "Enter current balance" else "Ange nuvarande saldo") },
            text = {
                OutlinedTextField(
                    value = customBalanceText,
                    onValueChange = { customBalanceText = it.filter(Char::isDigit) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    customBalanceText.toIntOrNull()?.let(onSetManualBalance)
                    showBalanceDialog = false
                }) { Text(if (isEnglish) "Save" else "Spara") }
            },
            dismissButton = {
                TextButton(onClick = { showBalanceDialog = false }) { Text(if (isEnglish) "Cancel" else "Avbryt") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (isEnglish) "Reset balance" else "Återställ saldo") },
            text = { Text(if (isEnglish) "This adds a correction transaction in history." else "Detta lägger till en korrigeringstransaktion i historiken.") },
            confirmButton = {
                TextButton(onClick = {
                    onResetBalance()
                    showResetDialog = false
                }) { Text(if (isEnglish) "Reset" else "Återställ") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(if (isEnglish) "Cancel" else "Avbryt") }
            }
        )
    }

    if (showThemeDialog) {
        Dialog(
            onDismissRequest = { showThemeDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121418)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "Choose theme" else "Välj tema",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppTheme.values().forEach { theme ->
                                val themeLabel = when (theme) {
                                    AppTheme.System -> "System"
                                    AppTheme.Signature -> "Signatur"
                                    else -> theme.name
                                }
                                Button(
                                    onClick = {
                                        onSetTheme(theme)
                                        showThemeDialog = false
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (theme == currentTheme) MaterialTheme.colorScheme.primary else Color(0xFF2A2F38),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Text(themeLabel, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showThemeDialog = false }) {
                                Text(
                                    text = if (isEnglish) "Close" else "Stäng",
                                    color = Color(0xFFFF3B30),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = startDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onSetPeriodDates(startDate, endDate)
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(if (isEnglish) "Cancel" else "Avbryt") }
            }
        ) { DatePicker(state = picker) }
    }

    if (showEndPicker) {
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = endDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let {
                        endDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onSetPeriodDates(startDate, endDate)
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text(if (isEnglish) "Cancel" else "Avbryt") }
            }
        ) { DatePicker(state = picker) }
    }

    if (showHolidayStartPicker) {
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = holidayStart
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showHolidayStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let {
                        holidayStart = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showHolidayStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showHolidayStartPicker = false }) { Text(if (isEnglish) "Cancel" else "Avbryt") }
            }
        ) { DatePicker(state = picker) }
    }

    if (showHolidayEndPicker) {
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = holidayEnd
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showHolidayEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let {
                        holidayEnd = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showHolidayEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showHolidayEndPicker = false }) { Text(if (isEnglish) "Cancel" else "Avbryt") }
            }
        ) { DatePicker(state = picker) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeRevealHolidayRow(
    holiday: Holiday,
    holidayDateFormatter: DateTimeFormatter,
    cardColor: Color,
    onDeleteHoliday: (Holiday) -> Unit
) {
    val maxDragPx = 168f
    val settledRevealPx = 132f
    var offsetX by remember(holiday.id) { mutableStateOf(0f) }
    var dragLock by remember(holiday.id) { mutableStateOf(0) } // -1 left-open, +1 right-open, 0 neutral
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 160),
        label = "holidayRevealOffset"
    )
    val showLeftDelete = animatedOffset >= 44f
    val showRightDelete = animatedOffset <= -44f
    var isRemoving by remember(holiday.id) { mutableStateOf(false) }

    LaunchedEffect(isRemoving) {
        if (isRemoving) {
            delay(180)
            onDeleteHoliday(holiday)
        }
    }

    val hs = Instant.ofEpochMilli(holiday.startDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val he = Instant.ofEpochMilli(holiday.endDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    AnimatedVisibility(
        visible = !isRemoving,
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showLeftDelete) {
                Box(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .width(48.dp)
                        .height(48.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(24.dp))
                        .clickable { isRemoving = true },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Radera lov",
                        tint = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { isRemoving = true }
                        )
                        .pointerInput(holiday.id) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragLock = when {
                                        offsetX < 0f -> -1
                                        offsetX > 0f -> 1
                                        else -> 0
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val proposed = offsetX + dragAmount
                                    offsetX = when (dragLock) {
                                        -1 -> proposed.coerceIn(-maxDragPx, 0f)
                                        1 -> proposed.coerceIn(0f, maxDragPx)
                                        else -> proposed.coerceIn(-maxDragPx, maxDragPx)
                                    }
                                },
                                onDragEnd = {
                                    dragLock = 0
                                    offsetX = when {
                                        offsetX <= -40f -> -settledRevealPx
                                        offsetX >= 40f -> settledRevealPx
                                        else -> 0f
                                    }
                                }
                            )
                        }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(holiday.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${hs.format(holidayDateFormatter)} - ${he.format(holidayDateFormatter)}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (showRightDelete) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .width(48.dp)
                        .height(48.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(24.dp))
                        .clickable { isRemoving = true },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Radera lov",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
    )
}

@Composable
private fun ValueRow(label: String, value: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp)
        }
    }
}

@Composable
private fun DateRow(label: String, value: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp)
            Box(
                modifier = Modifier
                    .background(Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp)
            Switch(
                checked = value,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .background(
                if (selected) Color(0x33FFFFFF) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}


