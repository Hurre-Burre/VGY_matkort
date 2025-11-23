package com.example.vgy_matkort.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.vgy_matkort.ui.theme.*

@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onDeleteTransaction: (Transaction) -> Unit,
    onRegisterHighlight: (String, Rect) -> Unit,
    isHapticEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                onRegisterHighlight("history_list", coordinates.boundsInRoot())
            }
    ) {
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Inga transaktioner ännu",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Dina utgifter kommer visas här",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions.sortedByDescending { it.timestamp }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { onDeleteTransaction(transaction) },
                        isHapticEnabled = isHapticEnabled
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit, isHapticEnabled: Boolean) {
    val haptic = LocalHapticFeedback.current
    
    // Format: "Mån 22 Nov" for date, "14:30" for time
    val dateFormat = SimpleDateFormat("EEE d MMM", Locale("sv", "SE"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale("sv", "SE"))
    val date = Date(transaction.timestamp)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Date and Time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormat.format(date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = TextTertiary
                )
            }
            
            // Middle: Amount
            Text(
                text = if (transaction.amount == 0) "0 kr" else "-${transaction.amount} kr",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = if (transaction.amount == 0) AccentGreen else PrimaryBlue,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Right side: Delete button with X
            IconButton(
                onClick = {
                    if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Ta bort",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
