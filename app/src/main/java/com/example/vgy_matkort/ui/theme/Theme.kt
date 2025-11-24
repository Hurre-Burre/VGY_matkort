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
    GradientColors(BlueGradientStart, BlueGradientMid, BackgroundGradientEnd)
}

@Composable
fun VGY_MatkortTheme(
    appTheme: AppTheme = AppTheme.Blue,
    darkTheme: Boolean = isSystemInDarkTheme(), // Kept for compatibility, but we enforce dark style mostly
    content: @Composable () -> Unit
) {
    val (colorScheme, gradientColors) = when (appTheme) {
        AppTheme.Blue -> Pair(
            darkColorScheme(primary = BluePrimary, surface = SurfaceDark, background = BlueGradientStart),
            GradientColors(BlueGradientStart, BlueGradientMid, BackgroundGradientEnd)
        )
        AppTheme.Green -> Pair(
            darkColorScheme(primary = GreenPrimary, surface = SurfaceDark, background = GreenGradientStart),
            GradientColors(GreenGradientStart, GreenGradientMid, BackgroundGradientEnd)
        )
        AppTheme.Red -> Pair(
            darkColorScheme(primary = RedPrimary, surface = SurfaceDark, background = RedGradientStart),
            GradientColors(RedGradientStart, RedGradientMid, BackgroundGradientEnd)
        )
        AppTheme.Orange -> Pair(
            darkColorScheme(primary = OrangePrimary, surface = SurfaceDark, background = OrangeGradientStart),
            GradientColors(OrangeGradientStart, OrangeGradientMid, BackgroundGradientEnd)
        )
        AppTheme.Purple -> Pair(
            darkColorScheme(primary = PurplePrimary, surface = SurfaceDark, background = PurpleGradientStart),
            GradientColors(PurpleGradientStart, PurpleGradientMid, BackgroundGradientEnd)
        )
        AppTheme.Pink -> Pair(
            darkColorScheme(primary = PinkPrimary, surface = SurfaceDark, background = PinkGradientStart),
            GradientColors(PinkGradientStart, PinkGradientMid, BackgroundGradientEnd)
        )
    }

    // Merge with common colors
    val finalColorScheme = colorScheme.copy(
        onPrimary = Color.White,
        onBackground = TextWhite,
        onSurface = TextWhite,
        surface = SurfaceDark // Ensure we use our transparent surface
    )

    CompositionLocalProvider(LocalGradientColors provides gradientColors) {
        MaterialTheme(
            colorScheme = finalColorScheme,
            typography = Typography,
            content = content
        )
    }
}