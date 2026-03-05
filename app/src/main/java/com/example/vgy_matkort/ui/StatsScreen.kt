package com.example.vgy_matkort.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Transaction
import com.example.vgy_matkort.ui.theme.iOSCardBackground
import java.time.LocalDate

@Composable
fun StatsScreen(
    uiState: UiState,
    transactions: List<Transaction>,
    onRegisterHighlight: (String, Rect) -> Unit = { _, _ -> }
) {
    val pieColors = listOf(Color(0xFF1E88E5), Color(0xFF38C45A), Color(0xFFFF8B34), Color(0xFFE15353))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Statistik",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                DonutChart(
                    values = uiState.restaurantExpenses.map { it.amount },
                    colors = pieColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    uiState.restaurantExpenses.take(2).forEachIndexed { index, item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .background(pieColors[index], CircleShape)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(item.name, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Sparade restauranger",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        uiState.restaurantExpenses.forEachIndexed { index, item ->
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(pieColors[index % pieColors.size], CircleShape)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(item.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                    }
                    Text("${item.amount} kr", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Veckototaler",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        uiState.weeklySummaries.forEach { summary ->
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("År ${LocalDate.now().year}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            "Vecka ${summary.weekNumber}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("${summary.balance} kr", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun DonutChart(values: List<Int>, colors: List<Color>, modifier: Modifier = Modifier) {
    val total = values.sum().coerceAtLeast(1)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(250.dp)) {
            var startAngle = -90f
            values.forEachIndexed { index, value ->
                val sweep = (value.toFloat() / total.toFloat()) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset.Zero,
                    size = size,
                    style = Stroke(width = 78f, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
    }
}

