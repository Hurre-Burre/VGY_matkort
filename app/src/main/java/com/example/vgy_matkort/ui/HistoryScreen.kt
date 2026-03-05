package com.example.vgy_matkort.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Transaction
import com.example.vgy_matkort.ui.theme.iOSCardBackground
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onDeleteTransaction: (Transaction) -> Unit,
    onClearAllTransactions: () -> Unit,
    onRegisterHighlight: (String, Rect) -> Unit = { _, _ -> },
    isHapticEnabled: Boolean,
    language: String = "sv"
) {
    val isEnglish = language == "en"
    val cardBg = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.5f) {
        Color(0x3A000000)
    } else {
        Color(0x33FFFFFF)
    }
    val visibleTransactions = transactions
        .filter { !it.isHidden && (it.amount != 0 || !it.description.isNullOrBlank()) }
        .sortedByDescending { it.timestamp }

    var showClearAllConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEnglish) "History" else "Historik",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { showClearAllConfirm = true }
                )
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(58.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = if (isEnglish) "Clear all" else "Rensa alla",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(14.dp))

        if (visibleTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isEnglish) "No history yet" else "Ingen historik ännu",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleTransactions, key = { it.id }) { tx ->
                    SwipeRevealHistoryCard(
                        transaction = tx,
                        language = language,
                        cardBackground = cardBg,
                        onDeleteRequested = { onDeleteTransaction(tx) }
                    )
                }
            }
        }
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(if (isEnglish) "Delete all transactions" else "Ta bort alla transaktioner") },
            text = { Text(if (isEnglish) "This cannot be undone." else "Detta går inte att ångra.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAllTransactions()
                    showClearAllConfirm = false
                }) {
                    Text(if (isEnglish) "Delete all" else "Ta bort alla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(if (isEnglish) "Cancel" else "Avbryt")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeRevealHistoryCard(
    transaction: Transaction,
    language: String,
    cardBackground: Color,
    onDeleteRequested: () -> Unit
) {
    val maxDragPx = 168f
    val settledRevealPx = 132f
    var offsetX by remember(transaction.id) { mutableStateOf(0f) }
    var dragLock by remember(transaction.id) { mutableStateOf(0) } // -1 left-open, +1 right-open, 0 neutral
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 160),
        label = "historyRevealOffset"
    )

    val showLeftDelete = animatedOffset >= 44f
    val showRightDelete = animatedOffset <= -44f
    var isRemoving by remember(transaction.id) { mutableStateOf(false) }

    LaunchedEffect(isRemoving) {
        if (isRemoving) {
            delay(180)
            onDeleteRequested()
        }
    }

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
                        .size(48.dp)
                        .background(Color(0xFFE53935), CircleShape)
                        .clickable { isRemoving = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Radera",
                        tint = Color.White
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                HistoryCard(
                    transaction = transaction,
                    language = language,
                    cardBackground = cardBackground,
                    onLongDelete = { isRemoving = true },
                    modifier = Modifier
                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                        .pointerInput(transaction.id) {
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
                )
            }

            if (showRightDelete) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(48.dp)
                        .background(Color(0xFFE53935), CircleShape)
                        .clickable { isRemoving = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Radera",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCard(
    transaction: Transaction,
    language: String,
    cardBackground: Color,
    onLongDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = if (language == "en") Locale.forLanguageTag("en-US") else Locale.forLanguageTag("sv-SE")
    val dateFormatter = SimpleDateFormat("d MMM yyyy HH:mm", locale)
    val amountText = if (transaction.amount == 0) "0 kr" else "-${transaction.amount} kr"
    val description = transaction.description ?: transaction.restaurantName

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongDelete)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = dateFormatter.format(Date(transaction.timestamp)),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "$amountText $description",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
