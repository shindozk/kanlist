package com.kanlist.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Kanlist — Kotlin + Material You recreation of the Expo app design. */
class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBars.apply(this)
        AppState.hydrate(this)

        root = FrameLayout(this).apply { setBackgroundColor(T.background(this@MainActivity)) }
        setContentView(root)
        render()
    }

    private fun render() {
        root.removeAllViews()
        if (!AppState.doc.settings.onboarded) {
            root.addView(OnboardingScreen(this) { AppState.updateSettings { it.onboarded = true }; AppState.save(this); render() })
            return
        }
        root.addView(MainScreen(this, currentTab) { tab ->
            currentTab = tab
            render()
        })
    }
}

/** Bottom-nav container with Home / Explore / Library / Profile. */
class MainScreen(
    private val ctx: MainActivity,
    private val tab: Int,
    private val onTab: (Int) -> Unit,
) : LinearLayout(ctx) {

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(T.background(ctx))

        val content = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        content.addView(when (tab) {
            1 -> ExploreScreen(ctx)
            2 -> LibraryScreen(ctx)
            3 -> ProfileScreen(ctx)
            else -> HomeScreen(ctx)
        })
        addView(content)

        addView(BottomBar(ctx, tab) { onTab(it) })
    }
}

/** Material-You bottom navigation bar. */
class BottomBar(ctx: MainActivity, tab: Int, private val onTab: (Int) -> Unit) : LinearLayout(ctx) {
    private val items = listOf(
        Triple("e88a", "Início", 0),
        Triple("e87a", "Explorar", 1),
        Triple("ea1c", "Biblioteca", 2),
        Triple("e7fd", "Perfil", 3),
    )

    init {
        orientation = LinearLayout.HORIZONTAL
        setBackgroundColor(T.card(ctx))
        elevation = T.dp(ctx, 8).toFloat()
        setPadding(0, T.dp(ctx, 8), 0, T.dp(ctx, 8))
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(ctx, 64))
        gravity = Gravity.CENTER_VERTICAL
        for ((i, item) in items.withIndex()) {
            val sel = i == tab
            val col = FrameLayout(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                isClickable = true
                setOnClickListener { onTab(i) }
            }
            val inner = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            val iconBg = FrameLayout(ctx).apply {
                background = T.round(ctx, if (sel) T.accent(ctx) else Color.TRANSPARENT, 16)
                setPadding(T.dp(ctx, 6), T.dp(ctx, 2), T.dp(ctx, 6), T.dp(ctx, 2))
            }
            iconBg.addView(T.icon(ctx, item.first, 22, if (sel) T.brown(ctx) else T.mutedFg(ctx)))
            inner.addView(iconBg)
            val label = T.text(ctx, item.second, 11, if (sel) T.brown(ctx) else T.mutedFg(ctx), bold = sel)
            label.gravity = Gravity.CENTER
            inner.addView(label)
            col.addView(inner, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
            addView(col)
        }
    }
}
