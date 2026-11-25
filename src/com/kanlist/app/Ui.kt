package com.kanlist.app

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/** Reusable UI primitives mirroring ui/primitives.tsx (Card, Pill, Chip, IconTile, T…). */
object Ui {

    fun vertical(ctx: Context): LinearLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
    }

    fun horizontal(ctx: Context): LinearLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    /** Card: white rounded surface with soft shadow. */
    fun card(ctx: Context, radius: Int = T.R_CARD, pad: Int = 0): LinearLayout {
        val v = vertical(ctx)
        v.background = T.round(ctx, T.card(ctx), radius)
        v.elevation = T.dp(ctx, 2).toFloat()
        if (pad > 0) v.setPadding(T.dp(ctx, pad), T.dp(ctx, pad), T.dp(ctx, pad), T.dp(ctx, pad))
        return v
    }

    fun spacer(ctx: Context, h: Int): View = View(ctx).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(ctx, h)) }

    fun hspacer(ctx: Context, w: Int): View = View(ctx).apply { layoutParams = LinearLayout.LayoutParams(T.dp(ctx, w), LinearLayout.LayoutParams.WRAP_CONTENT) }

    /** Section header (title + optional action). */
    fun sectionHeader(ctx: Context, title: String, action: String? = null, onAction: (() -> Unit)? = null): LinearLayout {
        val row = horizontal(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
        }
        val t = T.text(ctx, title, 19, T.foreground(ctx), bold = true)
        t.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(t)
        if (action != null) {
            val a = T.text(ctx, action, 13, T.foreground(ctx), bold = true).apply {
                setOnClickListener { onAction?.invoke() }
                setPadding(T.dp(ctx, 8), T.dp(ctx, 8), T.dp(ctx, 8), T.dp(ctx, 8))
            }
            row.addView(a)
        }
        return row
    }

    /** Pill button (brown / ink / soft / outline / danger). */
    fun pill(ctx: Context, label: String, variant: String = "brown", full: Boolean = false, enabled: Boolean = true, icon: String? = null, onClick: (() -> Unit)? = null): LinearLayout {
        val (bg, fg) = when (variant) {
            "ink" -> T.ink(ctx) to T.inkFg(ctx)
            "soft" -> T.accent(ctx) to T.accentFg(ctx)
            "outline" -> Color.TRANSPARENT to T.foreground(ctx)
            "danger" -> Color.TRANSPARENT to T.destructive(ctx)
            else -> T.brown(ctx) to T.brownFg(ctx)
        }
        val p = horizontal(ctx).apply {
            gravity = Gravity.CENTER
            background = if (variant == "outline" || variant == "danger") T.roundStroke(ctx, bg, fg, T.R_PILL) else T.round(ctx, bg, T.R_PILL)
            setPadding(T.dp(ctx, 20), 0, T.dp(ctx, 20), 0)
            layoutParams = LinearLayout.LayoutParams(if (full) LinearLayout.LayoutParams.MATCH_PARENT else LinearLayout.LayoutParams.WRAP_CONTENT, T.dp(ctx, 44))
            alpha = if (enabled) 1f else 0.5f
            isClickable = enabled
            if (enabled) setOnClickListener { onClick?.invoke() }
        }
        if (icon != null) {
            val ic = T.icon(ctx, icon, 17, fg)
            p.addView(ic)
            p.addView(hspacer(ctx, 8))
        }
        val t = T.text(ctx, label, 13, fg, bold = true)
        p.addView(t)
        return p
    }

    /** Chip: rounded 36dp with border; active = ink. */
    fun chip(ctx: Context, label: String, active: Boolean = false, icon: String? = null, onClick: (() -> Unit)? = null): LinearLayout {
        val bg = if (active) T.ink(ctx) else T.card(ctx)
        val fg = if (active) T.inkFg(ctx) else T.foreground(ctx)
        val p = horizontal(ctx).apply {
            gravity = Gravity.CENTER
            background = T.roundStroke(ctx, bg, if (active) T.ink(ctx) else T.border(ctx), T.R_PILL)
            setPadding(T.dp(ctx, 15), 0, T.dp(ctx, 15), 0)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, T.dp(ctx, 36))
            isClickable = true
            setOnClickListener { onClick?.invoke() }
        }
        if (icon != null) {
            p.addView(T.icon(ctx, icon, 15, fg))
            p.addView(hspacer(ctx, 6))
        }
        p.addView(T.text(ctx, label, 13, fg, bold = active))
        return p
    }

    /** Icon tile: pastel rounded square with icon. */
    fun iconTile(ctx: Context, icon: String, tile: String, size: Int = 44, radius: Int = T.R_TILE): FrameLayout {
        val (bg, fg) = T.tile(ctx, tile)
        val fl = FrameLayout(ctx).apply {
            background = T.round(ctx, bg, radius)
            layoutParams = LinearLayout.LayoutParams(T.dp(ctx, size), T.dp(ctx, size))
        }
        val ic = T.icon(ctx, icon, (size * 0.5).toInt(), fg)
        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
        fl.addView(ic, lp)
        return fl
    }

    fun iconButton(ctx: Context, icon: String, size: Int = 42, bg: Int? = null, fg: Int? = null, onClick: (() -> Unit)? = null): FrameLayout {
        val fl = FrameLayout(ctx).apply {
            background = T.round(ctx, bg ?: T.card(ctx), size / 2)
            elevation = T.dp(ctx, 2).toFloat()
            isClickable = true
            setOnClickListener { onClick?.invoke() }
        }
        val ic = T.icon(ctx, icon, (size * 0.48).toInt(), fg ?: T.foreground(ctx))
        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
        fl.addView(ic, lp)
        return fl
    }

    /** Progress bar: track + primary fill. */
    fun progressBar(ctx: Context, value: Float, height: Int = 6, color: Int? = null): View {
        val pct = value.coerceIn(0f, 1f)
        val track = FrameLayout(ctx).apply {
            background = T.round(ctx, T.track(ctx), T.R_PILL)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(ctx, height))
        }
        val fill = View(ctx).apply {
            background = T.round(ctx, color ?: T.primary(ctx), T.R_PILL)
            layoutParams = FrameLayout.LayoutParams((T.dp(ctx, height) * 0).coerceAtLeast(1), FrameLayout.LayoutParams.MATCH_PARENT)
        }
        track.addView(fill)
        track.post { fill.layoutParams.width = (track.width * pct).toInt().coerceAtLeast(if (pct > 0) T.dp(ctx, height) else 0) }
        return track
    }

    fun emptyState(ctx: Context, icon: String, title: String, text: String? = null, action: String? = null, tile: String = "peach", onAction: (() -> Unit)? = null): LinearLayout {
        val v = vertical(ctx).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(T.dp(ctx, 24), T.dp(ctx, 36), T.dp(ctx, 24), T.dp(ctx, 36))
        }
        v.addView(iconTile(ctx, icon, tile, 64, 22))
        v.addView(spacer(ctx, 10))
        val t = T.text(ctx, title, 16, T.foreground(ctx), bold = true).apply { gravity = Gravity.CENTER }
        v.addView(t)
        if (text != null) {
            v.addView(spacer(ctx, 6))
            val s = T.text(ctx, text, 13, T.mutedFg(ctx)).apply { gravity = Gravity.CENTER; maxLines = 3 }
            v.addView(s)
        }
        if (action != null) {
            v.addView(spacer(ctx, 12))
            v.addView(pill(ctx, action, onClick = onAction))
        }
        return v
    }

    fun errorNote(ctx: Context, message: String, onRetry: (() -> Unit)? = null): LinearLayout {
        val row = horizontal(ctx).apply {
            background = T.round(ctx, T.card(ctx), T.R_CARD)
            setPadding(T.dp(ctx, 16), T.dp(ctx, 16), T.dp(ctx, 16), T.dp(ctx, 16))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        row.addView(iconTile(ctx, "e2c1", "coral", 40))
        row.addView(hspacer(ctx, 12))
        val t = T.text(ctx, message, 13, T.foreground(ctx)).apply { layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        row.addView(t)
        if (onRetry != null) {
            row.addView(T.text(ctx, "Tentar", 13, T.primary(ctx), bold = true).apply { setOnClickListener { onRetry() } })
        }
        return row
    }
}
