package com.example.vgy_matkort.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// App Theme Enum
enum class AppTheme {
    Blue, Green, Red, Orange, Purple, Pink
}

// CompositionLocal for Gradient Colors
data class GradientColors(
    val start: Color,
    val mid: Color,
    val end: Color
)

val LocalGradientColors = androidx.compose.runtime.staticCompositionLocalOf {
    GradientColors(BlueGradientStart, BlueGradientMid, BlueGradientMid)
}

@Composable
fun VGY_MatkortTheme(
    appTheme: AppTheme = AppTheme.Blue,
    darkTheme: Boolean = isSystemInDarkTheme(), // Ignored, always forcing light for iOS look
    content: @Composable () -> Unit
) {
    val (colorScheme, gradientColors) = when (appTheme) {
        AppTheme.Blue -> Pair(
            lightColorScheme(primary = BluePrimary, surface = iOSCardBackground, background = iOSBackground),
            GradientColors(BlueGradientStart, BlueGradientMid, BlueGradientMid)
        )
        AppTheme.Green -> Pair(
            lightColorScheme(primary = GreenPrimary, surface = iOSCardBackground, background = iOSBackground),
            GradientColors(GreenGradientStart, GreenGradientMid, GreenGradientMid)
        )
        AppTheme.Red -> Pair(
            lightColorScheme(primary = RedPrimary, surface = iOSCardBackground, background = iOSBackground),
            GradientColors(RedGradientStart, RedGradientMid, RedGradientMid)
        )
        AppTheme.Orange -> Pair(
            lightColorScheme(primary = OrangePrimary, surface = iOSCardBackground, background = iOSBackground),
            GradientColors(OrangeGradientStart, OrangeGradientMid, OrangeGradientMid)
        )
        AppTheme.Purple -> Pair(
            lightColorScheme(primary = PurplePrimary, surface = iOSCardBackground, background = iOSBackground),
            GradientColors(PurpleGradientStart, PurpleGradientMid, PurpleGradientMid)
        )
        AppTheme.Pink -> Pair(
            lightColorScheme(primary = PinkPrimary, surface = iOSCardBackground, background = iOSBackground),
            GradientColors(PinkGradientStart, PinkGradientMid, PinkGradientMid)
        )
    }

    // Merge with common colors
    val finalColorScheme = colorScheme.copy(
        onPrimary = Color.White,
        onBackground = iOSTextBlack,
        onSurface = iOSTextBlack,
        surface = iOSCardBackground 
    )

    CompositionLocalProvider(LocalGradientColors provides gradientColors) {
        MaterialTheme(
            colorScheme = finalColorScheme,
            typography = Typography,
            content = content
        )
    }
}
