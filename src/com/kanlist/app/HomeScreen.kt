package com.kanlist.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

/** Home screen (mirror of app/(tabs)/index.tsx): greeting, week strip, hero, routine, FAB. */
class HomeScreen(ctx: Context) : FrameLayout(ctx) {

    private val io = Executors.newSingleThreadExecutor()
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    private val scroll = ScrollView(context)
    private val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private var selectedDay = Fmt.startOfDay(System.currentTimeMillis())

    init {
        setBackgroundColor(T.background(context))
        scroll.isVerticalScrollBarEnabled = false
        scroll.addView(content)
        addView(scroll, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        addView(Fab(context) {
            (context as? Activity)?.let { SearchActivity.open(it) }
        }, FrameLayout.LayoutParams(T.dp(context, 58), T.dp(context, 58), Gravity.BOTTOM or Gravity.END).apply {
            setMargins(T.dp(context, 22), 0, T.dp(context, 22), T.dp(context, 22))
        })
        build()
    }

    private fun build() {
        content.removeAllViews()
        val pad = T.dp(context, T.SCREEN)
        content.setPadding(pad, T.dp(context, 18), pad, T.dp(context, 100))

        val cal = java.util.Calendar.getInstance()
        content.addView(T.text(context, Fmt.greeting(cal), 30, T.foreground(context), bold = true))
        content.addView(T.text(context, Fmt.longDate(cal), 13, T.mutedFg(context)))
        content.addView(spacer(T.SECTION))

        // Week strip
        content.addView(WeekStrip(context, Fmt.weekOf(System.currentTimeMillis()), Fmt.dayKey(selectedDay)) { ts ->
            selectedDay = ts
            build()
        })
        content.addView(spacer(T.SECTION))

        if (AppState.doc.entries.isEmpty()) {
            content.addView(HeroCard(context) {
                (context as? Activity)?.let { SearchActivity.open(it) }
            })
            content.addView(spacer(T.SECTION))
            content.addView(Ui.sectionHeader(context, "Rotina de hoje"))
            content.addView(Ui.emptyState(context, "e2c3", "Nada na rotina ainda", "Obras marcadas como Assistindo ou Lendo aparecem aqui para você marcar o próximo episódio ou capítulo.", "Buscar obra", "lilac") {
                (context as? Activity)?.let { SearchActivity.open(it) }
            })
        } else {
            content.addView(goalCard())
            content.addView(spacer(T.SECTION))
            content.addView(Ui.sectionHeader(context, "Rotina de hoje"))
            val entries = AppState.doc.entries.values
                .filter { it.status == TrackingStatus.CURRENT || it.status == TrackingStatus.PLANNING }
                .sortedByDescending { it.updatedAt }
            if (entries.isEmpty()) {
                content.addView(Ui.emptyState(context, "e2c3", "Dia de descanso", "Nenhum episódio ou capítulo registrado nesse dia.", "Buscar obra", "lilac") {
                    (context as? Activity)?.let { SearchActivity.open(it) }
                })
            } else {
                entries.take(8).forEach { content.addView(RoutineRow(context, it)) }
            }
        }
    }

    private fun goalCard(): View {
        val today = Fmt.dayKey(System.currentTimeMillis())
        val unitsToday = AppState.doc.events.filter { Fmt.dayKey(it.at) == today }.sumOf { it.count }
        val goal = AppState.doc.settings.dailyGoal
        val card = Ui.card(context, pad = 16)
        card.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val row = Ui.horizontal(context)
        row.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val t = T.text(context, "Meta de hoje · $unitsToday/$goal", 15, T.foreground(context), bold = true)
        t.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(t)
        card.addView(row)
        card.addView(spacer(10))
        card.addView(Ui.progressBar(context, if (goal > 0) unitsToday.toFloat() / goal else 0f))
        return card
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}

/** Week strip: Seg…Dom with selectable circles. */
class WeekStrip(context: Context, days: List<Long>, selected: String, private val onSelect: (Long) -> Unit) : LinearLayout(context) {
    init {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val today = Fmt.dayKey(System.currentTimeMillis())
        for (d in days) {
            val k = Fmt.dayKey(d)
            val isSel = k == selected
            val isToday = k == today
            val col = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                isClickable = true
                setOnClickListener { onSelect(d) }
            }
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = d }
            val wd = T.text(context, Fmt.WEEKDAYS_SHORT[(cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7], 13, if (isToday) T.foreground(context) else T.mutedFg(context), bold = isToday)
            wd.gravity = Gravity.CENTER
            col.addView(wd)
            col.addView(spacer(10))
            val circle = FrameLayout(context).apply {
                background = T.round(context, if (isSel) T.ink(context) else T.card(context), 22)
                elevation = if (isSel) 0f else T.dp(context, 1).toFloat()
            }
            val num = T.text(context, cal.get(java.util.Calendar.DAY_OF_MONTH).toString(), 15, if (isSel) T.inkFg(context) else T.foreground(context), bold = isSel)
            num.gravity = Gravity.CENTER
            circle.addView(num, FrameLayout.LayoutParams(T.dp(context, 44), T.dp(context, 44)))
            col.addView(circle)
            addView(col)
        }
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(1, T.dp(context, h)) }
}

/** Peach hero card with CTA. */
class HeroCard(context: Context, onClick: () -> Unit) : LinearLayout(context) {
    init {
        orientation = LinearLayout.VERTICAL
        background = T.round(context, T.hero(context), T.R_HERO)
        setPadding(T.dp(context, 20), T.dp(context, 20), T.dp(context, 20), T.dp(context, 20))
        elevation = T.dp(context, 2).toFloat()
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        isClickable = true
        setOnClickListener { onClick() }
        addView(T.text(context, "Monte sua lista", 19, T.heroFg(context), bold = true))
        addView(spacer(6))
        addView(T.text(context, "Busque animes, mangás e manhwas e acompanhe cada episódio e capítulo.", 13, T.heroMuted(context)).apply { maxLines = 2 })
        addView(spacer(14))
        addView(Ui.pill(context, "Explorar", icon = "e5c8") { onClick() })
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}

/** Routine row: check circle + card with pastel tile + title + meta. */
class RoutineRow(context: Context, val entry: LibraryEntry) : LinearLayout(context) {
    init {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        setPadding(0, 0, 0, T.dp(context, T.GAP))

        val today = Fmt.dayKey(System.currentTimeMillis())
        val doneToday = AppState.doc.events.any { it.key == entry.key && Fmt.dayKey(it.at) == today }
        val check = FrameLayout(context).apply {
            background = T.roundStroke(context, if (doneToday) T.primary(context) else Color.TRANSPARENT, if (doneToday) T.primary(context) else T.border(context), 16, 2)
            layoutParams = LinearLayout.LayoutParams(T.dp(context, 32), T.dp(context, 32))
        }
        check.addView(T.icon(context, "e5ca", 16, if (doneToday) T.primaryFg(context) else T.mutedFg(context)), FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        addView(check)
        addView(Ui.hspacer(context, 12))

        val card = Ui.horizontal(context).apply {
            background = T.round(context, T.card(context), T.R_CARD)
            setPadding(T.dp(context, 10), T.dp(context, 10), T.dp(context, 10), T.dp(context, 10))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            elevation = T.dp(context, 1).toFloat()
        }
        card.addView(Ui.iconTile(context, entry.title.kind.icon, entry.title.kind.tile, 44))
        card.addView(Ui.hspacer(context, 12))
        val col = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        col.addView(T.text(context, Fmt.titleName(entry.title, AppState.doc.settings.titleLanguage), 16, T.foreground(context), bold = true).apply { maxLines = 2 })
        val next = AppState.nextUnit(entry)
        val subtitle = if (next != null) "${Fmt.unitShort(entry.title.medium)} $next de ${AppState.totalFor(entry) ?: "?"}" else "Tudo marcado"
        col.addView(T.text(context, subtitle, 13, T.mutedFg(context)))
        card.addView(col)
        addView(card)
    }
}

/** Floating action button. */
class Fab(context: Context, onClick: () -> Unit) : FrameLayout(context) {
    init {
        background = T.round(context, T.brown(context), 29)
        elevation = T.dp(context, 6).toFloat()
        isClickable = true
        setOnClickListener { onClick() }
        addView(T.icon(context, "e145", 28, T.brownFg(context)), FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }
}
