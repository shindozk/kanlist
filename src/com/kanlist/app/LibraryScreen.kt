package com.kanlist.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Library screen (mirror of app/(tabs)/library.tsx): search, filter, entry rows. */
class LibraryScreen(ctx: Context) : LinearLayout(ctx) {
    private val list = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private var query = ""
    private var kind = "all"

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(T.background(context))
        setPadding(T.dp(context, T.SCREEN), T.dp(context, 18), T.dp(context, T.SCREEN), 0)

        list.addView(T.text(context, "Biblioteca", 24, T.foreground(context), bold = true))
        list.addView(spacer(T.GAP))
        val search = android.widget.EditText(context).apply {
            hint = "Buscar na sua lista"; setTextSize(14f); setTextColor(T.foreground(context)); setHintTextColor(T.mutedFg(context))
            background = T.round(context, T.card(context), T.R_CARD); setPadding(T.dp(context, 14), 0, T.dp(context, 14), 0)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, 44))
            addTextChangedListener(afterTextChanged { q -> query = q ?: ""; rebuild() })
        }
        list.addView(search)
        list.addView(spacer(T.GAP))
        val filters = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        filters.addView(Ui.chip(context, "Todos", kind == "all") { kind = "all"; rebuild() })
        filters.addView(Ui.hspacer(context, 8))
        filters.addView(Ui.chip(context, "Animes", kind == Kind.ANIME.id) { kind = Kind.ANIME.id; rebuild() })
        filters.addView(Ui.hspacer(context, 8))
        filters.addView(Ui.chip(context, "Mangás", kind == Kind.MANGA.id) { kind = Kind.MANGA.id; rebuild() })
        list.addView(filters)
        list.addView(spacer(T.GAP))

        val scroll = ScrollView(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f); isVerticalScrollBarEnabled = false; addView(list) }
        addView(scroll)
        rebuild()
    }

    private fun rebuild() {
        list.removeAllViews()
        list.addView(T.text(context, "Biblioteca", 24, T.foreground(context), bold = true))
        list.addView(spacer(T.GAP))
        val entries = AppState.doc.entries.values
            .filter { kind == "all" || it.title.kind.id == kind }
            .filter { query.isBlank() || it.title.title.contains(query, true) }
            .sortedByDescending { it.updatedAt }
        if (entries.isEmpty()) {
            list.addView(Ui.emptyState(context, "ea1c", "Sua lista está vazia", "Busque animes, mangás e manhwas e adicione à biblioteca.", "Buscar obra", "lilac") {
                (context as? Activity)?.let { SearchActivity.open(it) }
            })
            return
        }
        for (e in entries) {
            val row = Ui.horizontal(context).apply {
                background = T.round(context, T.card(context), T.R_CARD)
                setPadding(T.dp(context, 10), T.dp(context, 10), T.dp(context, 10), T.dp(context, 10))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = T.dp(context, T.GAP) }
                elevation = T.dp(context, 1).toFloat(); isClickable = true
                setOnClickListener { DetailActivity.open(context, e.title) }
            }
            row.addView(CoverView(context, e.title, 48, 66))
            row.addView(Ui.hspacer(context, 12))
            val col = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
            col.addView(T.text(context, e.title.title, 15, T.foreground(context), bold = true).apply { maxLines = 1 })
            col.addView(T.text(context, Fmt.statusLabel(e.status, e.title.medium), 12, T.mutedFg(context)))
            val total = AppState.totalFor(e)
            val done = AppState.doneCount(e)
            if (total != null) col.addView(Ui.progressBar(context, done.toFloat() / total, 4).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, 4)).apply { topMargin = T.dp(context, 6) } })
            row.addView(col)
            list.addView(row)
        }
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}
