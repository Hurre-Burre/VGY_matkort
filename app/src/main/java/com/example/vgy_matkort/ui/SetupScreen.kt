package com.example.vgy_matkort.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.ui.theme.AppTheme
import com.example.vgy_matkort.ui.theme.iOSBackground
import com.example.vgy_matkort.ui.theme.iOSCardBackground
import com.example.vgy_matkort.ui.theme.iOSTextBlack
import com.example.vgy_matkort.ui.theme.iOSTextGray
import com.example.vgy_matkort.ui.theme.iOSTextLightGray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onCompleteSetup: (Int, Int, LocalDate, LocalDate, AppTheme, Int, Boolean) -> Unit,
    onNavigateToHolidays: () -> Unit
) {
    var balanceText by remember { mutableStateOf("") }
    var incomeText by remember { mutableStateOf("70") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(4)) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var reminderMinutes by remember { mutableStateOf(12 * 60) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("sv-SE"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(iOSBackground)
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(110.dp))

        Text(
            text = "Welcome",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = iOSTextBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set your period and your current balance",
            color = iOSTextGray,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp)) {
                FieldTitle("Current balance (kr)")
                InlineField(
                    value = balanceText,
                    onValueChange = { balanceText = it.filter(Char::isDigit) },
                    placeholder = "Ex: 1200"
                )

                Spacer(modifier = Modifier.height(18.dp))

                FieldTitle("Daily default budget (kr)")
                InlineField(
                    value = incomeText,
                    onValueChange = { incomeText = it.filter(Char::isDigit) },
                    placeholder = "70"
                )

                Spacer(modifier = Modifier.height(18.dp))

                DateField("Start date", startDate.format(formatter)) { _ -> showStartPicker = true }
                Spacer(modifier = Modifier.height(12.dp))
                DateField("End date", endDate.format(formatter)) { _ -> showEndPicker = true }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme", color = iOSTextBlack, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Signatur ⌄", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications", color = iOSTextBlack, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                DateField(
                    label = "Påminnelsetid",
                    value = "%02d:%02d".format(reminderMinutes / 60, reminderMinutes % 60)
                ) {
                    TimePickerDialog(
                        it,
                        { _, hour, minute -> reminderMinutes = hour * 60 + minute },
                        reminderMinutes / 60,
                        reminderMinutes % 60,
                        true
                    ).show()
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                onCompleteSetup(
                    balanceText.toIntOrNull() ?: 0,
                    incomeText.toIntOrNull() ?: 70,
                    startDate,
                    endDate,
                    AppTheme.Signature,
                    reminderMinutes,
                    notificationsEnabled
                )
            },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
        ) {
            Text("Continue", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        TextButton(onClick = onNavigateToHolidays) {
            Text("Hantera lov", color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showStartPicker) {
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Avbryt") } }
        ) {
            DatePicker(state = picker)
        }
    }

    if (showEndPicker) {
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let {
                        endDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Avbryt") } }
        ) {
            DatePicker(state = picker)
        }
    }
}

@Composable
private fun FieldTitle(text: String) {
    Text(text, color = iOSTextBlack, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun InlineField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = iOSTextLightGray) },
        trailingIcon = { Text("kr", color = iOSTextGray, fontSize = 16.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = iOSTextBlack,
            unfocusedTextColor = iOSTextBlack
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DateField(label: String, value: String, onClick: (android.content.Context) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(context) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = iOSTextBlack, fontSize = 18.sp)
        Box(
            modifier = Modifier
                .background(Color(0xFFE8E8ED), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(value, color = iOSTextBlack, fontSize = 18.sp)
        }
    }
}
