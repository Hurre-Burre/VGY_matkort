package com.example.vgy_matkort.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
            .padding(16.dp)
    ) {
        Text(
            text = "Saldoutveckling",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Faktiskt vs Ideal (70kr/dag)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .onGloballyPositioned { coordinates ->
                    onRegisterHighlight("stats_chart", coordinates.boundsInRoot())
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                BalanceChart(
                    data = uiState.chartData,
                    initialBalance = uiState.initialBalance,
                    totalDays = 100 // This could be improved to be dynamic
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Nuvarande saldo",
                value = "${uiState.currentBalance} kr",
                color = if (uiState.currentBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            StatItem(
                label = "Daglig budget",
                value = "${uiState.dailyAvailable} kr",
                color = MaterialTheme.colorScheme.secondary
            )
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
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            uiState.weeklySummaries.forEach { summary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vecka ${summary.weekNumber}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${summary.balance} kr",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BalanceChart(data: List<ChartPoint>, initialBalance: Int, totalDays: Int) {
    if (data.isEmpty()) return
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Margins for labels
        val marginLeft = 60f
        val marginBottom = 40f
        val marginTop = 20f
        val marginRight = 20f
        
        val chartWidth = width - marginLeft - marginRight
        val chartHeight = height - marginBottom - marginTop
        
        // Find min/max for scaling
        val maxBalance = data.maxOf { it.balance }.coerceAtLeast(100)
        val minBalance = data.minOf { it.balance }.coerceAtMost(-100)
        val range = maxBalance - minBalance
        
        val days = data.size
        val stepX = chartWidth / (days - 1).coerceAtLeast(1)
        
        // Helper to map value to Y coordinate (inverted, so max is at top)
        fun mapY(value: Int): Float {
            return marginTop + chartHeight - ((value - minBalance) / range.toFloat() * chartHeight)
        }
        
        // Helper to map X coordinate
        fun mapX(index: Int): Float {
            return marginLeft + index * stepX
        }
        
        // Draw grid lines (horizontal)
        val gridLines = 5
        for (i in 0..gridLines) {
            val value = minBalance + (range * i / gridLines)
            val y = mapY(value)
            
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(marginLeft, y),
                end = Offset(marginLeft + chartWidth, y),
                strokeWidth = 1f
            )
            
            // Y-axis labels
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText(
                    "${value}kr",
                    marginLeft - 10f,
                    y + 5f,
                    paint
                )
            }
        }
        
        // Draw vertical grid lines
        val verticalGridLines = minOf(days - 1, 10)
        for (i in 0..verticalGridLines) {
            val index = (days - 1) * i / verticalGridLines
            val x = mapX(index)
            
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(x, marginTop),
                end = Offset(x, marginTop + chartHeight),
                strokeWidth = 1f
            )
            
            // X-axis labels (day numbers)
            if (i % 2 == 0 || verticalGridLines <= 5) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(
                        "Dag ${index + 1}",
                        x,
                        marginTop + chartHeight + 30f,
                        paint
                    )
                }
            }
        }
        
        // Draw Zero Line (Ideal)
        val zeroY = mapY(0)
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        
        drawLine(
            color = Color(0xFF4CAF50), // Green
            start = Offset(marginLeft, zeroY),
            end = Offset(marginLeft + chartWidth, zeroY),
            strokeWidth = 3f,
            pathEffect = dashPathEffect
        )
        
        // Draw Actual Balance Path
        val path = Path()
        data.forEachIndexed { index, point ->
            val x = mapX(index)
            val y = mapY(point.balance)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = Color(0xFF2196F3),
            style = Stroke(width = 4f)
        )
        
        // Draw data points as circles
        data.forEachIndexed { index, point ->
            val x = mapX(index)
            val y = mapY(point.balance)
            
            // Determine color based on balance
            val pointColor = when {
                point.balance >= 100 -> Color(0xFF4CAF50)
                point.balance >= 0 -> Color(0xFF8BC34A)
                point.balance >= -100 -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }
            
            drawCircle(
                color = pointColor,
                radius = 6f,
                center = Offset(x, y)
            )
            
            // White border around circle
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}
