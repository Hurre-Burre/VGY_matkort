package com.example.vgy_matkort.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Holiday
import com.example.vgy_matkort.ui.components.AddHolidayDialog
import com.example.vgy_matkort.ui.theme.iOSBackground
import com.example.vgy_matkort.ui.theme.iOSBlue
import com.example.vgy_matkort.ui.theme.iOSCardBackground
import com.example.vgy_matkort.ui.theme.iOSTextBlack
import com.example.vgy_matkort.ui.theme.iOSTextGray
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHolidaysScreen(
    holidays: List<Holiday>,
    onAddHoliday: (Long, Long, String) -> Unit,
    onDeleteHoliday: (Holiday) -> Unit,
    onImportHolidays: suspend () -> Result<Int>,
    onBack: () -> Unit,
    isHapticEnabled: Boolean
) {
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var holidayToDelete by remember { mutableStateOf<Holiday?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    if (showAddDialog) {
        AddHolidayDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { start, end, name ->
                onAddHoliday(start, end, name)
                showAddDialog = false
            },
            isHapticEnabled = isHapticEnabled
        )
    }

    if (holidayToDelete != null) {
        AlertDialog(
            onDismissRequest = { holidayToDelete = null },
            title = { Text("Ta bort lov") },
            text = { Text("Vill du ta bort '${holidayToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    holidayToDelete?.let(onDeleteHoliday)
                    holidayToDelete = null
                }) { Text("Ta bort") }
            },
            dismissButton = {
                TextButton(onClick = { holidayToDelete = null }) { Text("Avbryt") }
            }
        )
    }

    if (message != null) {
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("Import") },
            text = { Text(message!!) },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text("OK") }
            }
        )
    }

    Scaffold(
        containerColor = iOSBackground,
        topBar = {
            TopAppBar(
                title = { Text("Hantera lov", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = iOSBackground),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka", tint = iOSBlue)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val result = onImportHolidays()
                            message = result.fold(
                                onSuccess = { if (it > 0) "Importerade $it lov" else "Inga nya lov att importera" },
                                onFailure = { "Import misslyckades: ${it.message}" }
                            )
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Importera", tint = iOSBlue)
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Lägg till", tint = iOSBlue)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(iOSBackground)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(holidays) { holiday ->
                HolidayRow(holiday = holiday, onDelete = { holidayToDelete = holiday })
            }
        }
    }
}

@Composable
private fun HolidayRow(holiday: Holiday, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("sv-SE"))
    val start = Instant.ofEpochMilli(holiday.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val end = Instant.ofEpochMilli(holiday.endDate).atZone(ZoneId.systemDefault()).toLocalDate()

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(holiday.name, color = iOSTextBlack, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("${start.format(formatter)} - ${end.format(formatter)}", color = iOSTextGray, fontSize = 14.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Ta bort", tint = iOSTextGray)
            }
        }
    }
}
