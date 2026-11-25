package com.kanlist.app

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Onboarding screen (mirror of app/onboarding.tsx). */
class OnboardingScreen(ctx: Context, private val onDone: () -> Unit) : ScrollView(ctx) {

    private val interests = AppState.doc.settings.interests.toMutableList()
    private var goal = AppState.doc.settings.dailyGoal
    private val nameInput = EditText(ctx)

    init {
        setBackgroundColor(T.background(ctx))
        isVerticalScrollBarEnabled = false
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(T.dp(ctx, T.SCREEN), T.dp(ctx, 28), T.dp(ctx, T.SCREEN), T.dp(ctx, T.SCREEN))
        }

        // Mascot circle
        val mascot = FrameLayout(ctx).apply {
            background = T.round(ctx, T.hero(ctx), 56)
            elevation = T.dp(ctx, 3).toFloat()
            layoutParams = LinearLayout.LayoutParams(T.dp(ctx, 112), T.dp(ctx, 112)).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
        mascot.addView(T.icon(ctx, "e8d1", 40, T.brown(ctx)), FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        col.addView(mascot)
        col.addView(spacer(14))

        col.addView(T.text(ctx, "Bem-vindo ao Kanlist", 24, T.foreground(ctx), bold = true).apply { gravity = Gravity.CENTER })
        col.addView(spacer(6))
        col.addView(T.text(ctx, "Sua rotina de animes, mangás, manhwas e donghuas — episódio por episódio, capítulo por capítulo.", 13, T.mutedFg(ctx)).apply { gravity = Gravity.CENTER; maxLines = 3 })
        col.addView(spacer(26))

        col.addView(T.text(ctx, "Como podemos te chamar?", 19, T.foreground(ctx), bold = true))
        col.addView(spacer(10))
        nameInput.apply {
            hint = "Seu nome ou apelido"
            setTextColor(T.foreground(ctx))
            setHintTextColor(T.mutedFg(ctx))
            setTextSize(16f)
            maxLines = 1
            imeOptions = EditorInfo.IME_ACTION_DONE
            background = T.round(ctx, T.card(ctx), T.R_CARD)
            setPadding(T.dp(ctx, 18), 0, T.dp(ctx, 18), 0)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(ctx, 52))
            elevation = T.dp(ctx, 1).toFloat()
        }
        col.addView(nameInput)
        col.addView(spacer(26))

        col.addView(T.text(ctx, "O que você acompanha?", 19, T.foreground(ctx), bold = true))
        col.addView(spacer(10))
        val chips = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        // flow-ish layout using rows
        var row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.START }
        for (k in Kind.entries) {
            val chip = Ui.chip(ctx, k.plural, interests.contains(k), k.icon) {
                if (interests.contains(k)) interests.remove(k) else interests.add(k)
                refreshChips(row, chips)
            }
            row.addView(chip)
            row.addView(Ui.hspacer(ctx, 8))
            if ((row.childCount) > 6) { chips.addView(row); row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.START } }
        }
        chips.addView(row)
        col.addView(chips)
        col.addView(spacer(26))

        col.addView(T.text(ctx, "Meta diária", 19, T.foreground(ctx), bold = true))
        col.addView(spacer(6))
        col.addView(T.text(ctx, "Quantos episódios ou capítulos por dia mantêm sua sequência viva?", 13, T.mutedFg(ctx)))
        col.addView(spacer(10))
        val goals = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        for (g in listOf(1, 3, 5, 10)) {
            goals.addView(Ui.chip(ctx, g.toString(), goal == g) { goal = g; refreshChips(goals, null, g) })
            goals.addView(Ui.hspacer(ctx, 8))
        }
        col.addView(goals)
        col.addView(spacer(28))

        col.addView(Ui.pill(ctx, "Começar", icon = "e5c8", full = true) {
            AppState.updateSettings {
                it.name = nameInput.text.toString().trim()
                it.interests = if (interests.isNotEmpty()) interests else listOf(Kind.ANIME, Kind.MANGA)
                it.dailyGoal = goal
            }
            AppState.save(ctx)
            onDone()
        })

        addView(col)
    }

    private fun refreshChips(row: LinearLayout, container: LinearLayout?, goal: Int? = null) {
        container?.requestLayout()
        invalidate()
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}
