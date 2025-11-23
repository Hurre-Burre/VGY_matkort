package com.example.vgy_matkort.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vgy_matkort.ui.theme.TextWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHolidayDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, String) -> Unit,
    isHapticEnabled: Boolean
) {
    var step by remember { mutableIntStateOf(1) } // 1 = start date, 2 = end date, 3 = name
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    
    val haptic = LocalHapticFeedback.current
    val datePickerState = rememberDatePickerState()
    
    // Reset date picker selection when moving between steps if needed
    LaunchedEffect(step) {
        if (step == 1 && startDateMillis != null) {
            datePickerState.selectedDateMillis = startDateMillis
        } else if (step == 2) {
            datePickerState.selectedDateMillis = endDateMillis ?: startDateMillis // Default to start date if end not set
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (step > 1) {
                            step--
                        } else {
                            onDismiss()
                        }
                    }) {
                        Icon(
                            imageVector = if (step > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = if (step > 1) "Tillbaka" else "Stäng",
                            tint = TextWhite
                        )
                    }
                    Text(
                        text = when (step) {
                            1 -> "Startdatum"
                            2 -> "Slutdatum"
                            else -> "Namn på lov"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(modifier = Modifier.size(48.dp))
                }

                if (step == 1 || step == 2) {
                    // Date Selection Steps
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.Transparent,
                                titleContentColor = TextWhite,
                                headlineContentColor = TextWhite,
                                weekdayContentColor = TextWhite.copy(alpha = 0.6f),
                                subheadContentColor = TextWhite,
                                yearContentColor = TextWhite,
                                selectedDayContainerColor = TextWhite,
                                selectedDayContentColor = Color.Black,
                                dayContentColor = TextWhite,
                                todayContentColor = TextWhite,
                                todayDateBorderColor = TextWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Name Input Step
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (name.isEmpty()) "Namn" else name,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (name.isEmpty()) TextWhite.copy(alpha = 0.3f) else TextWhite,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(2.dp)
                                .background(TextWhite.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "t.ex. Sportlov, Påsklov",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite.copy(alpha = 0.5f)
                        )
                    }
                    
                    // Hidden TextField for keyboard input
                    androidx.compose.foundation.text.BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .offset(y = (-200).dp), // Hide it visually but keep it focusable
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(TextWhite)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Action Button
                Button(
                    onClick = {
                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        when (step) {
                            1 -> {
                                datePickerState.selectedDateMillis?.let {
                                    startDateMillis = it
                                    step = 2
                                }
                            }
                            2 -> {
                                datePickerState.selectedDateMillis?.let {
                                    endDateMillis = it
                                    step = 3
                                }
                            }
                            3 -> {
                                if (name.isNotBlank() && startDateMillis != null && endDateMillis != null) {
                                    // Ensure start is before end
                                    val start = minOf(startDateMillis!!, endDateMillis!!)
                                    val end = maxOf(startDateMillis!!, endDateMillis!!)
                                    onConfirm(start, end, name)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextWhite,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = when (step) {
                        1 -> datePickerState.selectedDateMillis != null
                        2 -> datePickerState.selectedDateMillis != null
                        3 -> name.isNotBlank()
                        else -> false
                    }
                ) {
                    Text(
                        text = if (step == 3) "Spara" else "Nästa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Add some bottom padding for the keyboard
                if (step == 3) {
                    Spacer(modifier = Modifier.height(300.dp)) // Push content up for keyboard
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
