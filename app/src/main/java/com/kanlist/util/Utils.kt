package com.kanlist.util

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.kanlist.domain.model.ThemeMode

object ThemeUtils {
    fun getSystemTheme(context: Context): ThemeMode {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return when (nightMode) {
            Configuration.UI_MODE_NIGHT_YES -> ThemeMode.DARK
            Configuration.UI_MODE_NIGHT_NO -> ThemeMode.LIGHT
            else -> ThemeMode.SYSTEM
        }
    }

    fun applyThemeToSystemBars(activity: android.app.Activity, theme: ThemeMode) {
        val window = activity.window
        val isDark = when (theme) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> getSystemTheme(activity) == ThemeMode.DARK
        }
        
        window.decorView.systemUiVisibility = if (isDark) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}

object HapticUtils {
    fun performHapticFeedback(view: View, type: Int = android.view.HapticFeedbackConstants.VIRTUAL_KEY) {
        view.performHapticFeedback(type)
    }
}

object DateUtils {
    fun formatDate(date: java.time.LocalDate): String {
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date)
    }

    fun formatDateTime(dateTime: java.time.LocalDateTime): String {
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dateTime)
    }

    fun timeAgo(dateTime: java.time.LocalDateTime): String {
        val now = java.time.LocalDateTime.now()
        val diff = java.time.Duration.between(dateTime, now)
        
        return when {
            diff.toDays() > 0 -> "${diff.toDays()} dia${if (diff.toDays() > 1) "s" else ""} atrás"
            diff.toHours() > 0 -> "${diff.toHours()} hora${if (diff.toHours() > 1) "s" else ""} atrás"
            diff.toMinutes() > 0 -> "${diff.toMinutes()} min atrás"
            else -> "Agora mesmo"
        }
    }
}

object StringUtils {
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) text else text.substring(0, maxLength - 3) + "..."
    }

    fun capitalizeFirst(text: String): String {
        return if (text.isEmpty()) text else text.replaceFirstChar { it.uppercase() }
    }
}

object ColorUtils {
    fun getContrastColor(backgroundColor: Int): Int {
        val r = (backgroundColor shr 16) and 0xFF
        val g = (backgroundColor shr 8) and 0xFF
        val b = backgroundColor and 0xFF
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
        return if (luminance > 0.5) 0xFF000000 else 0xFFFFFFFF
    }
}