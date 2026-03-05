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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgy_matkort.data.Restaurant
import com.example.vgy_matkort.ui.theme.iOSCardBackground
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RestaurantsScreen(
    restaurants: List<Restaurant>,
    onAddRestaurant: (String) -> Unit,
    onDeleteRestaurant: (Restaurant) -> Unit,
    isHapticEnabled: Boolean,
    language: String = "sv"
) {
    val isEnglish = language == "en"
    val cardBg = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.5f) {
        Color(0x3A000000)
    } else {
        Color(0x33FFFFFF)
    }
    var name by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val saveRestaurant = {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onAddRestaurant(trimmed)
            name = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = if (isEnglish) "Restaurants" else "Restauranger",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = if (isEnglish) "Saved restaurants" else "Sparade restauranger",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(restaurants, key = { it.id }) { restaurant ->
                SwipeRevealRestaurantRow(
                    restaurant = restaurant,
                    onDeleteRestaurant = onDeleteRestaurant,
                    isHapticEnabled = isHapticEnabled,
                    cardBackground = cardBg
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = if (isEnglish) "Add" else "Lägg till",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(
                        text = if (isEnglish) "New restaurant" else "Ny restaurang",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { saveRestaurant() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = iOSCardBackground,
                    unfocusedContainerColor = iOSCardBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            )

            Button(
                onClick = { saveRestaurant() },
                enabled = name.trim().isNotEmpty(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier.height(72.dp)
            ) {
                Text(
                    text = if (isEnglish) "Save" else "Spara",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeRevealRestaurantRow(
    restaurant: Restaurant,
    onDeleteRestaurant: (Restaurant) -> Unit,
    isHapticEnabled: Boolean,
    cardBackground: Color
) {
    val maxDragPx = 168f
    val settledRevealPx = 132f
    var offsetX by remember(restaurant.id) { mutableStateOf(0f) }
    var dragLock by remember(restaurant.id) { mutableStateOf(0) } // -1 left-open, +1 right-open, 0 neutral
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 160),
        label = "restaurantRevealOffset"
    )

    val haptic = LocalHapticFeedback.current
    val showLeftDelete = animatedOffset >= 44f
    val showRightDelete = animatedOffset <= -44f
    var isRemoving by remember(restaurant.id) { mutableStateOf(false) }

    LaunchedEffect(isRemoving) {
        if (isRemoving) {
            delay(180)
            onDeleteRestaurant(restaurant)
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
                        .clickable {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isRemoving = true
                        },
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
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isRemoving = true
                            }
                        )
                        .pointerInput(restaurant.id) {
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
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = restaurant.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            if (showRightDelete) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(48.dp)
                        .background(Color(0xFFE53935), CircleShape)
                        .clickable {
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isRemoving = true
                        },
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
