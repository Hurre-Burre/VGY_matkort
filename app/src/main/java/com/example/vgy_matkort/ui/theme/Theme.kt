package com.example.vgy_matkort.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider

// App Theme Enum
enum class AppTheme {
    System, Signature, Blue, Green, Red, Orange, Purple, Pink
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
    appTheme: AppTheme = AppTheme.Signature,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val (colorScheme, gradientColors) = when (appTheme) {
        AppTheme.System -> {
            if (darkTheme) {
                Pair(
                    darkColorScheme(
                        primary = Color(0xFF68D77E),   // signature green accent (dark-adapted)
                        secondary = Color(0xFFE2B84E), // signature yellow accent (dark-adapted)
                        tertiary = Color(0xFFE2B84E),
                        background = Color(0xFF181811),
                        surface = Color(0xFF26261B),
                        onBackground = Color.White,
                        onSurface = Color.White
                    ),
                    GradientColors(Color(0xFF191810), Color(0xFF232317), Color(0xFF1A2A1C))
                )
            } else {
                Pair(
                    lightColorScheme(
                        primary = Color(0xFF3AAA4D),   // signature green accent
                        secondary = Color(0xFFF1C24B), // signature yellow accent
                        tertiary = Color(0xFFF1C24B),
                        background = Color(0xFFF2EFE7), // poster-like warm light background
                        surface = Color(0xFFF7F5EF),
                        onBackground = Color(0xFF101114),
                        onSurface = Color(0xFF101114)
                    ),
                    GradientColors(Color(0xFFF5F2E8), Color(0xFFF0ECCF), Color(0xFFE6F2DE))
                )
            }
        }
        AppTheme.Signature -> Pair(
            lightColorScheme(
                primary = Color(0xFF2EA84A),
                secondary = Color(0xFFF1C24B),
                tertiary = Color(0xFFF1C24B),
                surface = Color(0xFFF7F5EF),
                background = Color(0xFFF2EFE7)
            ),
            GradientColors(Color(0xFFF5F2E8), Color(0xFFF0ECCF), Color(0xFFE6F2DE))
        )
        AppTheme.Blue -> Pair(
            lightColorScheme(
                primary = BluePrimary,
                surface = iOSCardBackground,
                background = Color(0xFF0F2B56),
                onBackground = Color.White,
                onSurface = Color.White
            ),
            GradientColors(BlueGradientStart, BlueGradientMid, BlueGradientMid)
        )
        AppTheme.Green -> Pair(
            lightColorScheme(
                primary = GreenPrimary,
                surface = iOSCardBackground,
                background = Color(0xFF143F2D),
                onBackground = Color.White,
                onSurface = Color.White
            ),
            GradientColors(GreenGradientStart, GreenGradientMid, GreenGradientMid)
        )
        AppTheme.Red -> Pair(
            lightColorScheme(
                primary = RedPrimary,
                surface = iOSCardBackground,
                background = Color(0xFF4A1C1C),
                onBackground = Color.White,
                onSurface = Color.White
            ),
            GradientColors(RedGradientStart, RedGradientMid, RedGradientMid)
        )
        AppTheme.Orange -> Pair(
            lightColorScheme(
                primary = OrangePrimary,
                surface = iOSCardBackground,
                background = Color(0xFF5A2A14),
                onBackground = Color.White,
                onSurface = Color.White
            ),
            GradientColors(OrangeGradientStart, OrangeGradientMid, OrangeGradientMid)
        )
        AppTheme.Purple -> Pair(
            lightColorScheme(
                primary = PurplePrimary,
                surface = iOSCardBackground,
                background = Color(0xFF31204D),
                onBackground = Color.White,
                onSurface = Color.White
            ),
            GradientColors(PurpleGradientStart, PurpleGradientMid, PurpleGradientMid)
        )
        AppTheme.Pink -> Pair(
            lightColorScheme(
                primary = PinkPrimary,
                surface = iOSCardBackground,
                background = Color(0xFF4A203A),
                onBackground = Color.White,
                onSurface = Color.White
            ),
            GradientColors(PinkGradientStart, PinkGradientMid, PinkGradientMid)
        )
    }

    // Merge with common colors
    val finalColorScheme = colorScheme.copy(
        onPrimary = Color.White,
        onBackground = if (appTheme == AppTheme.System) {
            if (darkTheme) Color.White else Color(0xFF101114)
        } else if (appTheme == AppTheme.Signature) {
            Color(0xFF101114)
        } else {
            Color.White
        },
        onSurface = if (appTheme == AppTheme.System) {
            if (darkTheme) Color.White else Color(0xFF101114)
        } else if (appTheme == AppTheme.Signature) {
            Color(0xFF101114)
        } else {
            Color.White
        },
        surface = when {
            appTheme == AppTheme.System && darkTheme -> Color(0xFF17181B)
            appTheme == AppTheme.System && !darkTheme -> Color(0xCCFFFFFF)
            appTheme == AppTheme.Signature -> Color(0xCCFFFFFF)
            else -> Color(0x4A000000)
        }
    )

    CompositionLocalProvider(LocalGradientColors provides gradientColors) {
        MaterialTheme(
            colorScheme = finalColorScheme,
            typography = Typography,
            content = content
        )
    }
}
