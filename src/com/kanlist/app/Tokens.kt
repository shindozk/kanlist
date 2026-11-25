package com.kanlist.app

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/** Kanlist design tokens (mirror of constants/theme.ts) + Material You helpers. */
object T {
    const val SCREEN = 22
    const val GAP = 12
    const val SECTION = 28
    const val R_CARD = 22
    const val R_HERO = 26
    const val R_TILE = 14
    const val R_PILL = 999

    fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun c(ctx: Context, name: String): Int =
        ctx.resources.getColor(ctx.resources.getIdentifier(name, "color", ctx.packageName), ctx.theme)

    fun background(ctx: Context) = c(ctx, "background")
    fun foreground(ctx: Context) = c(ctx, "foreground")
    fun card(ctx: Context) = c(ctx, "card")
    fun primary(ctx: Context) = c(ctx, "primary")
    fun primaryFg(ctx: Context) = c(ctx, "primary_foreground")
    fun mutedFg(ctx: Context) = c(ctx, "muted_foreground")
    fun border(ctx: Context) = c(ctx, "border")
    fun ink(ctx: Context) = c(ctx, "ink")
    fun inkFg(ctx: Context) = c(ctx, "ink_foreground")
    fun brown(ctx: Context) = c(ctx, "brown")
    fun brownFg(ctx: Context) = c(ctx, "brown_foreground")
    fun hero(ctx: Context) = c(ctx, "hero")
    fun heroFg(ctx: Context) = c(ctx, "hero_foreground")
    fun heroMuted(ctx: Context) = c(ctx, "hero_muted")
    fun accent(ctx: Context) = c(ctx, "accent")
    fun accentFg(ctx: Context) = c(ctx, "accent_foreground")
    fun track(ctx: Context) = c(ctx, "track")
    fun destructive(ctx: Context) = c(ctx, "destructive")

    val TILES = listOf("peach", "honey", "coral", "rose", "sage", "sky", "lilac", "sand", "mint", "blush")

    fun tile(ctx: Context, name: String): Pair<Int, Int> {
        val safe = if (name in TILES) name else "peach"
        return c(ctx, "tile_${safe}_bg") to c(ctx, "tile_${safe}_fg")
    }

    fun text(ctx: Context, s: String, sizeSp: Int, color: Int, bold: Boolean = false): TextView =
        TextView(ctx).apply {
            text = s
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp.toFloat())
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
        }

    fun round(ctx: Context, color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(ctx, radius).toFloat() }

    fun roundStroke(ctx: Context, fill: Int, stroke: Int, radius: Int, width: Int = 1): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            setStroke(dp(ctx, width), stroke)
            cornerRadius = dp(ctx, radius).toFloat()
        }

    fun icon(ctx: Context, code: String, sizeDp: Int, color: Int): ImageView =
        ImageView(ctx).apply {
            setImageResource(iconRes(code))
            setColorFilter(color)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            val d = dp(ctx, sizeDp)
            minimumWidth = d
            minimumHeight = d
        }

    /** Map a Material Symbols codepoint to a drawable resource (vector). */
    fun iconRes(code: String): Int = when (code) {
        "e88a" -> R.drawable.ic_home
        "e87a" -> R.drawable.ic_explore
        "ea1c" -> R.drawable.ic_library
        "e7fd" -> R.drawable.ic_profile
        "e8b6" -> R.drawable.ic_search
        "e145" -> R.drawable.ic_add
        "e5c4" -> R.drawable.ic_arrow_back
        "e5c8" -> R.drawable.ic_arrow_forward
        "e5cd" -> R.drawable.ic_close
        "e5ca" -> R.drawable.ic_check
        "e865" -> R.drawable.ic_book
        "e502" -> R.drawable.ic_tv
        "e3a0" -> R.drawable.ic_film
        "e8d1" -> R.drawable.ic_sparkle
        "e8ce" -> R.drawable.ic_flower
        "e324" -> R.drawable.ic_phone
        "e8d5" -> R.drawable.ic_menu_book
        "e873" -> R.drawable.ic_description
        "ea0b" -> R.drawable.ic_bolt
        "e3ae" -> R.drawable.ic_brush
        "e838" -> R.drawable.ic_star
        "e8b5" -> R.drawable.ic_clock
        "e935" -> R.drawable.ic_calendar
        "e53d" -> R.drawable.ic_local_fire
        "e2c1" -> R.drawable.ic_cloud_off
        "e2c3" -> R.drawable.ic_bedtime
        "e8b8" -> R.drawable.ic_settings
        "e40a" -> R.drawable.ic_palette
        "e3a6" -> R.drawable.ic_dark
        "e430" -> R.drawable.ic_light
        "e894" -> R.drawable.ic_language
        "e8e2" -> R.drawable.ic_translate
        "e7f4" -> R.drawable.ic_notifications
        "e4b2" -> R.drawable.ic_vibration
        "e1db" -> R.drawable.ic_storage
        "e94c" -> R.drawable.ic_backup
        "e2c4" -> R.drawable.ic_download
        "e2c6" -> R.drawable.ic_upload
        "e92b" -> R.drawable.ic_delete
        "e3c9" -> R.drawable.ic_edit
        "e5d4" -> R.drawable.ic_more
        "e866" -> R.drawable.ic_bookmark
        "e87d" -> R.drawable.ic_favorite
        "e153" -> R.drawable.ic_flag
        "e042" -> R.drawable.ic_replay
        "e037" -> R.drawable.ic_play
        "e034" -> R.drawable.ic_pause
        "e86c" -> R.drawable.ic_check_circle
        "e03e" -> R.drawable.ic_radio
        "e887" -> R.drawable.ic_help
        "e89d" -> R.drawable.ic_open
        "e157" -> R.drawable.ic_link
        "e7fb" -> R.drawable.ic_people
        "e41d" -> R.drawable.ic_style
        "e429" -> R.drawable.ic_tune
        "e164" -> R.drawable.ic_sort
        "e4d6" -> R.drawable.ic_filter
        "e9cc" -> R.drawable.ic_grid
        "e9ca" -> R.drawable.ic_list
        "e15b" -> R.drawable.ic_remove
        "e031" -> R.drawable.ic_new_releases
        "e8e5" -> R.drawable.ic_trending
        "e889" -> R.drawable.ic_history
        "e24b" -> R.drawable.ic_bar_chart
        else -> R.drawable.ic_help
    }
}
