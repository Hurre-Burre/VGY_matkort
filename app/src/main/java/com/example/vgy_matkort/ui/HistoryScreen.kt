package com.example.vgy_matkort.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vgy_matkort.data.Transaction
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.IsoFields
import androidx.compose.ui.geometry.Rect
import java.util.Locale
import java.util.Date
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onDeleteTransaction: (Transaction) -> Unit,
    onRegisterHighlight: (String, Rect) -> Unit,
    isHapticEnabled: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                onRegisterHighlight("history_list", coordinates.boundsInRoot())
            },
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        val groupedTransactions = transactions.groupBy { transaction ->
            val date = Instant.ofEpochMilli(transaction.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val weekFields = IsoFields.WEEK_OF_WEEK_BASED_YEAR
            val week = date.get(weekFields)
            "Vecka $week"
        }

        groupedTransactions.forEach { (week, weekTransactions) ->
            item {
                WeekHeader(week = week, total = weekTransactions.sumOf { it.amount })
            }
            items(weekTransactions) { transaction ->
                TransactionItem(
                    transaction = transaction, 
                    onDelete = { onDeleteTransaction(transaction) },
                    isHapticEnabled = isHapticEnabled
                )
            }
        }
        
        if (transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Inga transaktioner ännu", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WeekHeader(week: String, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = week,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Total: $total kr",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider()
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit, isHapticEnabled: Boolean) {
    val haptic = LocalHapticFeedback.current
    // Updated format to include Day Name (e.g. "Mån, 22 Nov, 14:30")
    val dateFormat = SimpleDateFormat("EEE, d MMM, HH:mm", Locale("sv", "SE"))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val amountText = if (transaction.amount == 0) "Skipped Meal" else "-${transaction.amount} kr"
                Text(text = amountText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = dateFormat.format(Date(transaction.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = {
                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
