package com.example.vgy_matkort.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.vgy_matkort.data.Transaction
import java.time.Instant
import java.time.LocalDate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlin.math.pow
import com.example.vgy_matkort.ui.theme.*

@Composable
fun StatsScreen(
    uiState: UiState,
    transactions: List<Transaction>,
    onRegisterHighlight: (String, Rect) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Header
        Text(
            text = "Statistik",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Översikt av ditt saldo",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            color = TextTertiary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Stats Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Nuvarande",
                value = "${uiState.currentBalance} kr",
                color = if (uiState.currentBalance >= 0) AccentGreen else AccentRed,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Per dag",
                value = "${uiState.dailyAvailable} kr",
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f) // Responsive height
                .onGloballyPositioned { coordinates ->
                    onRegisterHighlight("stats_chart", coordinates.boundsInRoot())
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Saldoutveckling",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Faktiskt vs Ideal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxSize()) {
                    ModernBalanceChart(
                        data = uiState.chartData,
                        initialBalance = uiState.initialBalance,
                        totalDays = 100
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Weekly Breakdown
        Column(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                onRegisterHighlight("stats_weekly", coordinates.boundsInRoot())
            }
        ) {
            Text(
                text = "Veckovis uppdelning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            uiState.weeklySummaries.forEach { summary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vecka ${summary.weekNumber}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                        Text(
                            text = "${summary.balance} kr",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (summary.balance >= 0) AccentGreen else AccentRed
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = color
            )
        }
    }
}

@Composable
fun ModernBalanceChart(data: List<ChartPoint>, initialBalance: Int, totalDays: Int) {
    if (data.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ingen data tillgänglig",
                color = TextTertiary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }
    

    
    val haptic = LocalHapticFeedback.current
    val primaryColor = MaterialTheme.colorScheme.primary
    var touchedIndex by remember { mutableStateOf<Int?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Calculate which data point is being touched
                            val marginLeft = 50f
                            val marginRight = 10f
                            val chartWidth = size.width - marginLeft - marginRight
                            val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
                            
                            val relativeX = offset.x - marginLeft
                            val index = (relativeX / stepX).toInt().coerceIn(0, data.size - 1)
                            touchedIndex = index
                        },
                        onDrag = { change, _ ->
                            val marginLeft = 50f
                            val marginRight = 10f
                            val chartWidth = size.width - marginLeft - marginRight
                            val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
                            
                            val relativeX = change.position.x - marginLeft
                            val index = (relativeX / stepX).toInt().coerceIn(0, data.size - 1)
                            
                            if (touchedIndex != index) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                touchedIndex = index
                            }
                        },
                        onDragEnd = {
                            touchedIndex = null
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            
            // Margins
            val marginLeft = 50f
            val marginBottom = 30f
            val marginTop = 10f
            val marginRight = 10f
            
            val chartWidth = width - marginLeft - marginRight
            val chartHeight = height - marginBottom - marginTop
            
            // Find min/max for scaling
            val maxBalance = data.maxOf { it.balance }
            val minBalance = data.minOf { it.balance }
            val dataRange = maxBalance - minBalance
            
            // Calculate nice round numbers for Y-axis
            fun calculateNiceInterval(range: Int): Int {
                val roughInterval = range / 4 // We want about 4-5 grid lines
                val magnitude = 10.0.pow(kotlin.math.floor(kotlin.math.log10(roughInterval.toDouble()))).toInt()
                val normalized = roughInterval / magnitude
                
                return when {
                    normalized <= 1 -> magnitude
                    normalized <= 2 -> 2 * magnitude
                    normalized <= 5 -> 5 * magnitude
                    else -> 10 * magnitude
                }
            }
            
            val interval = if (dataRange > 0) calculateNiceInterval(dataRange) else 50
            val niceMin = (minBalance / interval) * interval - interval
            val niceMax = ((maxBalance / interval) + 1) * interval + interval
            val range = niceMax - niceMin
            
            val days = data.size
            val stepX = chartWidth / (days - 1).coerceAtLeast(1)
            
            // Helper functions
            fun mapY(value: Int): Float {
                return marginTop + chartHeight - ((value - niceMin) / range.toFloat() * chartHeight)
            }
            
            fun mapX(index: Int): Float {
                return marginLeft + index * stepX
            }
            
            // Draw subtle grid lines (horizontal only) with nice intervals
            var currentValue = niceMin
            while (currentValue <= niceMax) {
                val y = mapY(currentValue)
                
                drawLine(
                    color = TextTertiary.copy(alpha = 0.1f),
                    start = Offset(marginLeft, y),
                    end = Offset(marginLeft + chartWidth, y),
                    strokeWidth = 1f
                )
                
                // Y-axis labels
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = TextTertiary.copy(alpha = 0.6f).toArgb()
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    drawText(
                        "${currentValue}kr",
                        marginLeft - 8f,
                        y + 8f,
                        paint
                    )
                }
                
                currentValue += interval
            }
            
            // Draw zero line (ideal balance)
            if (niceMin <= 0 && niceMax >= 0) {
                val zeroY = mapY(0)
                drawLine(
                    color = AccentGreen.copy(alpha = 0.3f),
                    start = Offset(marginLeft, zeroY),
                    end = Offset(marginLeft + chartWidth, zeroY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
            
            // Draw the main balance line
            val path = Path()
            data.forEachIndexed { index, point ->
                val x = mapX(index)
                val y = mapY(point.balance)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3f)
            )
            
            // Draw touch indicator if touching
            touchedIndex?.let { index ->
                if (index in data.indices) {
                    val x = mapX(index)
                    val y = mapY(data[index].balance)
                    
                    // Vertical line
                    drawLine(
                        color = TextWhite.copy(alpha = 0.3f),
                        start = Offset(x, marginTop),
                        end = Offset(x, marginTop + chartHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                    
                    // Circle at data point
                    drawCircle(
                        color = primaryColor,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        // Tooltip overlay
        touchedIndex?.let { index ->
            if (index in data.indices) {
                val point = data[index]
                
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale("sv", "SE"))
                        Text(
                            text = point.date.format(dateFormatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${point.balance} kr",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (point.balance >= 0) AccentGreen else AccentRed,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
