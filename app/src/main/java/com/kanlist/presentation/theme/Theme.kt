package com.kanlist.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.kanlist.presentation.ui.theme.KanlistTheme

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8B2E), // Orange caramel
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C2A0D), // Brown chocolate
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFD4A574), // Warm beige
    onSecondary = Color(0xFF1E1B19),
    secondaryContainer = Color(0xFF3A2214),
    onSecondaryContainer = Color(0xFFFFE3CF),
    tertiary = Color(0xFFB24668), // Doujin pink
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3A141E),
    onTertiaryContainer = Color(0xFFF7DDE4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF151210), // Warm dark brown
    onBackground = Color(0xFFF4ECE6),
    surface = Color(0xFF211C19), // Card background
    onSurface = Color(0xFFF4ECE6),
    surfaceVariant = Color(0xFF2E2723),
    onSurfaceVariant = Color(0xFF9E948D),
    outline = Color(0xFF2E2723),
    outlineVariant = Color(0xFF2E2723),
    shadow = Color.Black,
    scrim = Color.Black,
    inverseSurface = Color(0xFFF4ECE6),
    inverseOnSurface = Color(0xFF151210),
    inversePrimary = Color(0xFF5C2A0D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF07C1F), // Orange caramel
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC4), // Peach
    onPrimaryContainer = Color(0xFF3A2214),
    secondary = Color(0xFF8A5A2B), // Novel brown
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E6D8),
    onSecondaryContainer = Color(0xFF3A2214),
    tertiary = Color(0xFFC2449B), // Donghua pink
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBD5EE),
    onTertiaryContainer = Color(0xFF3A141E),
    error = Color(0xFFD9534F),
    onError = Color.White,
    errorContainer = Color(0xFFFFD9D2),
    onErrorContainer = Color(0xFF3A141E),
    background = Color(0xFFF6F1EE), // Cream pinkish
    onBackground = Color(0xFF1E1B19),
    surface = Color.White, // White cards
    onSurface = Color(0xFF1E1B19),
    surfaceVariant = Color(0xFFEEE6E1),
    onSurfaceVariant = Color(0xFF8E8782),
    outline = Color(0xFFEEE6E1),
    outlineVariant = Color(0xFFEEE6E1),
    shadow = Color(0x0F5C2A0D),
    scrim = Color(0x0F5C2A0D),
    inverseSurface = Color(0xFF1E1B19),
    inverseOnSurface = Color(0xFFF6F1EE),
    inversePrimary = Color(0xFFFFDCC4)
)

// Type-specific colors for media tiles
object MediaTileColors {
    // Light theme colors (background, icon)
    val light = mapOf(
        com.kanlist.domain.model.MediaType.ANIME to Pair(Color(0xFFFFDCC4), Color(0xFFE8741A)),
        com.kanlist.domain.model.MediaType.MOVIE to Pair(Color(0xFFFDE3A7), Color(0xFFB7791F)),
        com.kanlist.domain.model.MediaType.OVA to Pair(Color(0xFFFFD9D2), Color(0xFFD9534F)),
        com.kanlist.domain.model.MediaType.DONGHUA to Pair(Color(0xFFFBD5EE), Color(0xFFC2449B)),
        com.kanlist.domain.model.MediaType.MANGA to Pair(Color(0xFFE3EFCB), Color(0xFF5E8B23)),
        com.kanlist.domain.model.MediaType.MANHWA to Pair(Color(0xFFD7E9F7), Color(0xFF2F7BBF)),
        com.kanlist.domain.model.MediaType.MANHUA to Pair(Color(0xFFE6DEFA), Color(0xFF6C4FD0)),
        com.kanlist.domain.model.MediaType.NOVEL to Pair(Color(0xFFF3E6D8), Color(0xFF8A5A2B)),
        com.kanlist.domain.model.MediaType.ONE_SHOT to Pair(Color(0xFFDDF1EC), Color(0xFF2C8C78)),
        com.kanlist.domain.model.MediaType.DOUJIN to Pair(Color(0xFFF7DDE4), Color(0xFFB24668)),
    )

    // Dark theme colors (background with 18% opacity, icon)
    val dark = mapOf(
        com.kanlist.domain.model.MediaType.ANIME to Pair(Color(0x2EE8741A), Color(0xFFE8741A)),
        com.kanlist.domain.model.MediaType.MOVIE to Pair(Color(0x2EB7791F), Color(0xFFB7791F)),
        com.kanlist.domain.model.MediaType.OVA to Pair(Color(0x2ED9534F), Color(0xFFD9534F)),
        com.kanlist.domain.model.MediaType.DONGHUA to Pair(Color(0x2EC2449B), Color(0xFFC2449B)),
        com.kanlist.domain.model.MediaType.MANGA to Pair(Color(0x2E5E8B23), Color(0xFF5E8B23)),
        com.kanlist.domain.model.MediaType.MANHWA to Pair(Color(0x2E2F7BBF), Color(0xFF2F7BBF)),
        com.kanlist.domain.model.MediaType.MANHUA to Pair(Color(0x2E6C4FD0), Color(0xFF6C4FD0)),
        com.kanlist.domain.model.MediaType.NOVEL to Pair(Color(0x2E8A5A2B), Color(0xFF8A5A2B)),
        com.kanlist.domain.model.MediaType.ONE_SHOT to Pair(Color(0x2E2C8C78), Color(0xFF2C8C78)),
        com.kanlist.domain.model.MediaType.DOUJIN to Pair(Color(0x2EB24668), Color(0xFFB24668)),
    )

    fun getBackground(type: com.kanlist.domain.model.MediaType, isDark: Boolean): Color {
        return if (isDark) dark[type]?.first ?: Color.Transparent else light[type]?.first ?: Color.Transparent
    }

    fun getIcon(type: com.kanlist.domain.model.MediaType, isDark: Boolean): Color {
        return if (isDark) dark[type]?.second ?: Color.White else light[type]?.second ?: Color.White
    }
}

@Composable
fun KanlistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    val context = LocalContext.current

    // Apply system bar colors
    SideEffect {
        val activity = (context as? Activity) ?: return@SideEffect
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()
        
        val isLight = !darkTheme
        window.decorView.systemUiVisibility = if (isLight) {
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else 0
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

val Typography = androidx.compose.material3.Typography(
    displayLarge = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = -0.5.sp,
        lineHeight = 36.sp
    ),
    displayMedium = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    displaySmall = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineLarge = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp
    ),
    headlineMedium = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    headlineSmall = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    titleLarge = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleMedium = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    titleSmall = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = androidx.compose.material3.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

val Shapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp), // Tile
    medium = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), // Card
    large = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), // Hero card
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp), // Bottom sheet
    extraExtraLarge = androidx.compose.foundation.shape.RoundedCornerShape(999.dp) // Pill
)

// Extension to get media tile colors
@Composable
fun MediaTileColors(
    type: com.kanlist.domain.model.MediaType,
    isDark: Boolean = MaterialTheme.colorScheme.isDark
): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return MediaTileColors.getBackground(type, isDark) to MediaTileColors.getIcon(type, isDark)
}

// Helper to get media tile background color
@Composable
fun MediaTileBackgroundColor(
    type: com.kanlist.domain.model.MediaType,
    isDark: Boolean = MaterialTheme.colorScheme.isDark
): androidx.compose.ui.graphics.Color {
    return MediaTileColors.getBackground(type, isDark)
}

// Helper to get media tile icon color
@Composable
fun MediaTileIconColor(
    type: com.kanlist.domain.model.MediaType,
    isDark: Boolean = MaterialTheme.colorScheme.isDark
): androidx.compose.ui.graphics.Color {
    return MediaTileColors.getIcon(type, isDark)
}