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
import androidx.compose.material.icons.filled.Delete
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
import java.util.Locale
import java.util.Date
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.vgy_matkort.ui.theme.*

@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onDeleteTransaction: (Transaction) -> Unit,
    onRegisterHighlight: (String, Rect) -> Unit = { _, _ -> },
    isHapticEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(iOSBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historik",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = iOSTextBlack
                )
                
                // Keep the 'Clear All' looking button from the screenshot just as a UI element or for individual deletes
                // If it's a clear all button in the design, maybe log something or delete all. But user said "no functional changes".
                // We'll leave it out if we don't have the function, or make it a dummy button that is disabled.
                IconButton(
                    onClick = { /* TODO: Clear all */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(iOSCardBackground, CircleShape),
                    enabled = false
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Rensa historik",
                        tint = iOSBlue.copy(alpha = 0.5f) // disabled look
                    )
                }
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Inga transaktioner ännu",
                            style = MaterialTheme.typography.titleMedium,
                            color = iOSTextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Dina utgifter kommer visas här",
                            style = MaterialTheme.typography.bodyMedium,
                            color = iOSTextGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filter out balance corrections (amount == 0) unless they have a description, and hidden transactions
                    val displayTransactions = transactions.filter { (it.amount != 0 || it.description != null) && !it.isHidden }
                    items(displayTransactions.sortedByDescending { it.timestamp }) { transaction ->
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
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit, isHapticEnabled: Boolean) {
    val haptic = LocalHapticFeedback.current
    
    // Format: "4 mars 2026 15:01"
    val dateFormat = SimpleDateFormat("d MMM yyyy HH:mm", Locale("sv", "SE"))
    val date = Date(transaction.timestamp)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = iOSTextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val amountText = if (transaction.amount == 0) "0 kr" else "-${transaction.amount} kr"
                val descText = transaction.description ?: ""
                val displayText = if (descText.isNotEmpty()) "$amountText $descText" else amountText
                
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = iOSTextBlack
                )
            }
            
            // Delete button mapping as "X" or "Trash" if we must keep it per-item (functional requirement)
            IconButton(
                onClick = {
                    if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(iOSBackground, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Ta bort",
                    tint = iOSTextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
