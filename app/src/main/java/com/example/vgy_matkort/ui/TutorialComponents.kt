package com.example.vgy_matkort.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

data class TutorialStepData(
    val title: String,
    val description: String,
    val highlightArea: HighlightArea? = null,
    val route: String = "home",
    val tankViewPage: Int? = null
) {
    companion object {
        val steps = listOf(
            TutorialStepData(
                title = "Välkommen till VGY Matkort",
                description = "Här får du en överblick över ditt saldo. Denna mätare visar hur mycket du har kvar totalt att spendera.",
                highlightArea = HighlightArea.TANK_VIEW,
                route = "home",
                tankViewPage = 0
            ),
            TutorialStepData(
                title = "Daglig Budget",
                description = "Swipa för att se din dagliga budget. Vi räknar ut hur mycket du kan spendera per dag baserat på kvarvarande skoldagar.",
                highlightArea = HighlightArea.TANK_VIEW,
                route = "home",
                tankViewPage = 1
            ),
            TutorialStepData(
                title = "Veckobudget",
                description = "Här ser du hur du ligger till denna vecka. Grönt betyder att du ligger bra till!",
                highlightArea = HighlightArea.TANK_VIEW,
                route = "home",
                tankViewPage = 2
            ),
            TutorialStepData(
                title = "Periodbudget",
                description = "En översikt för hela perioden fram till nästa lov. Planera dina inköp smart!",
                highlightArea = HighlightArea.TANK_VIEW,
                route = "home",
                tankViewPage = 3
            ),
            TutorialStepData(
                title = "Snabbval",
                description = "Lägg snabbt till köp med dessa knappar. Perfekt när du står i kassan!",
                highlightArea = HighlightArea.QUICK_ADD,
                route = "home"
            ),
            TutorialStepData(
                title = "Förinställningar",
                description = "Spara dina vanligaste köp här, t.ex. 'Kaffe' eller 'Mellanmål'. Tryck på + för att skapa en ny.",
                highlightArea = HighlightArea.PRESETS_LIST,
                route = "home"
            ),

            TutorialStepData(
                title = "Historik",
                description = "Här kan du se alla dina tidigare transaktioner och ta bort felaktiga köp.",
                highlightArea = HighlightArea.HISTORY_LIST,
                route = "history"
            ),
            TutorialStepData(
                title = "Statistik",
                description = "Följ din saldoutveckling över tid med en graf. Se hur ditt saldo förändras dag för dag.",
                highlightArea = HighlightArea.STATS_CHART,
                route = "stats"
            ),
            TutorialStepData(
                title = "Veckovis uppdelning",
                description = "Här ser du en sammanställning för varje vecka. Klicka på en vecka för att se detaljer.",
                highlightArea = HighlightArea.STATS_WEEKLY,
                route = "stats"
            ),
            TutorialStepData(
                title = "Inställningar",
                description = "Här kan du ändra tema (mörkt/ljust), hantera lovdagar och justera ditt saldo manuellt.",
                highlightArea = HighlightArea.SETTINGS_NAV_ITEM,
                route = "settings"
            )
        )
    }
}

enum class BannerPosition {
    TOP,
    BOTTOM
}

enum class HighlightArea(val key: String) {
    TANK_VIEW("tank_view"),
    QUICK_ADD("quick_add_buttons"),
    PRESETS_LIST("presets_list"),
    SETTINGS_THEME("settings_theme"),
    SETTINGS_HOLIDAYS("settings_holidays"),
    SETTINGS_DATA("settings_data"),
    SETTINGS_NAV_ITEM("settings_nav_item"),
    SETTINGS_SCREEN("settings_screen"),
    HISTORY_LIST("history_list"),
    STATS_CHART("stats_chart"),
    STATS_WEEKLY("stats_weekly")
}

@Composable
fun TutorialBanner(
    currentStep: Int,
    totalSteps: Int,
    title: String,
    description: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentStep) 12.dp else 8.dp)
                            .background(
                                color = if (index == currentStep) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkip()
                    }) { 
                        Text("Tillbaka")
                    }
                } else {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkip()
                    }) {
                        Text("Hoppa över")
                    }
                }
                
                Button(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNext()
                }) {
                    Text(if (currentStep < totalSteps - 1) "Nästa" else "Klar")
                }
            }
        }
    }
}

data class HighlightSpec(
    val rect: Rect,
    val cornerRadius: androidx.compose.ui.unit.Dp
)

@Composable
fun TutorialOverlayWithDim(
    currentStep: Int,
    totalSteps: Int,
    title: String,
    description: String,
    highlightSpecs: List<HighlightSpec>,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenHeightPx = with(density) { screenHeight.toPx() }

    // Calculate dynamic banner position based on highlight
    // If highlight is in the bottom half of the screen, put banner at TOP.
    // Otherwise put banner at BOTTOM.
    val calculatedBannerPosition = remember(highlightSpecs, screenHeightPx) {
        if (highlightSpecs.isNotEmpty()) {
            val highlightCenterY = highlightSpecs.first().rect.center.y
            if (highlightCenterY > screenHeightPx / 2) {
                BannerPosition.TOP
            } else {
                BannerPosition.BOTTOM
            }
        } else {
            BannerPosition.BOTTOM // Default
        }
    }

    // Smooth animated vertical bias for banner position
    // -1f is Top, 1f is Bottom
    val verticalBias by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (calculatedBannerPosition == BannerPosition.TOP) -0.9f else 0.9f, // Slightly off-edge
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "banner_bias"
    )
    
    var overlayOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    
    // Paint for soft cutout
    val cutoutPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            // DST_OUT removes the destination (background) based on alpha
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT)
            // Blur mask for soft edges
            // SOLID means the inside is solid (alpha 1) and edges are blurred
            maskFilter = android.graphics.BlurMaskFilter(30f, android.graphics.BlurMaskFilter.Blur.SOLID)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
            .onGloballyPositioned { coordinates ->
                overlayOffset = coordinates.positionInRoot()
            }
            // Swipe detection
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { },
                    onHorizontalDrag = { change: PointerInputChange, dragAmount: Float ->
                        if (dragAmount < -20f) { // Swipe Left -> Next
                            onNext()
                        } else if (dragAmount > 20f) { // Swipe Right -> Back
                            if (currentStep > 0) {
                                onBack()
                            }
                        }
                    }
                )
            }
    ) {
        // Semi-transparent overlay with cutout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Use offscreen compositing to allow BlendMode.DstOut to work properly
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    // Draw the content (which is empty for this Box, but we could have children)
                    drawContent()
                    
                    // Draw the dimmed background
                    drawRect(color = Color.Black.copy(alpha = 0.7f))
                    
                    // Cut out the highlight areas using native canvas for BlurMaskFilter support
                    drawIntoCanvas { canvas ->
                        highlightSpecs.forEach { spec ->
                            val cornerRadiusPx = with(density) { spec.cornerRadius.toPx() }
                            // Adjust rect position by subtracting the overlay's offset
                            val adjustedLeft = spec.rect.left - overlayOffset.x
                            val adjustedTop = spec.rect.top - overlayOffset.y
                            val adjustedRight = adjustedLeft + spec.rect.width
                            val adjustedBottom = adjustedTop + spec.rect.height
                            
                            canvas.nativeCanvas.drawRoundRect(
                                adjustedLeft,
                                adjustedTop,
                                adjustedRight,
                                adjustedBottom,
                                cornerRadiusPx,
                                cornerRadiusPx,
                                cutoutPaint
                            )
                        }
                    }
                }
                // Block clicks on the dimmed area but allow swipes to pass through to parent
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { 
                            // Consume taps
                        }
                    )
                }
        )
        
        // Tutorial banner with animated alignment
        // We use a Box that respects system insets so the banner isn't covered by nav bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
            contentAlignment = BiasAlignment(0f, verticalBias)
        ) {
            TutorialBanner(
                currentStep = currentStep,
                totalSteps = totalSteps,
                title = title,
                description = description,
                onNext = onNext,
                onSkip = if (currentStep > 0) onBack else onSkip, // Pass onBack if not first step
                modifier = Modifier.pointerInput(Unit) {
                    // Consume touches on the banner so they don't trigger swipes underneath if not desired
                }
            )
        }
    }
}

