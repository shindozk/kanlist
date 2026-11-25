package com.kanlist.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

/** Search screen (mirror of app/search.tsx). */
class SearchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBars.apply(this)
        AppState.hydrate(this)
        val root = FrameLayout(this).apply { setBackgroundColor(T.background(this@SearchActivity)) }
        setContentView(root)
        root.addView(SearchScreen(this) { finish() })
    }

    companion object {
        fun open(c: Context) { (c as? Activity)?.startActivity(Intent(c, SearchActivity::class.java)) }
    }
}

class SearchScreen(ctx: Context, private val onBack: () -> Unit) : LinearLayout(ctx) {
    private val io = Executors.newSingleThreadExecutor()
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    private val input = EditText(context)
    private val results = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private var filter = "all"

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(T.background(context))
        setPadding(0, T.dp(context, 18), 0, 0)

        val top = Ui.horizontal(context).apply {
            setPadding(T.dp(context, T.SCREEN), 0, T.dp(context, T.SCREEN), 0)
        }
        top.addView(Ui.iconButton(context, "e5c4", onClick = onBack))
        input.apply {
            hint = "Buscar obra"
            setTextColor(T.foreground(context)); setHintTextColor(T.mutedFg(context)); setTextSize(16f)
            background = T.round(context, T.card(context), T.R_CARD)
            setPadding(T.dp(context, 16), 0, T.dp(context, 16), 0)
            layoutParams = LinearLayout.LayoutParams(0, T.dp(context, 46), 1f).apply { marginStart = T.dp(context, 12) }
            maxLines = 1
            addTextChangedListener(afterTextChanged { q -> search(q ?: "") })
        }
        top.addView(input)
        addView(top)
        addView(spacer(T.GAP))

        val filters = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(T.dp(context, T.SCREEN), 0, T.dp(context, T.SCREEN), 0)
        }
        filters.addView(Ui.chip(context, "Todos", filter == "all") { filter = "all"; search(input.text.toString()) })
        filters.addView(Ui.hspacer(context, 8))
        filters.addView(Ui.chip(context, "Animes", filter == Kind.ANIME.id) { filter = Kind.ANIME.id; search(input.text.toString()) })
        filters.addView(Ui.hspacer(context, 8))
        filters.addView(Ui.chip(context, "Mangás", filter == Kind.MANGA.id) { filter = Kind.MANGA.id; search(input.text.toString()) })
        addView(filters)
        addView(spacer(T.GAP))

        val scroll = ScrollView(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f); isVerticalScrollBarEnabled = false }
        scroll.addView(results)
        addView(scroll)
    }

    private fun search(q: String) {
        results.removeAllViews()
        if (q.trim().length < 2) {
            results.addView(Ui.emptyState(context, "e8b6", "O que vamos acompanhar?", "Buscamos ao mesmo tempo no AniList, MangaDex, Kitsu e MyAnimeList (Jikan)."))
            return
        }
        results.addView(T.text(context, "Buscando…", 13, T.mutedFg(context)).apply { setPadding(T.dp(context, T.SCREEN), T.dp(context, 8), 0, 0) })
        io.execute {
            val res = Catalog.search(q, filter, AppState.doc.settings.showAdult)
            main.post {
                results.removeAllViews()
                if (res.items.isEmpty()) {
                    results.addView(Ui.emptyState(context, "e8b6", "Nada encontrado", "Tente outro nome — o romaji ou o título em inglês costumam funcionar melhor."))
                    return@post
                }
                res.items.take(30).forEach { t -> results.addView(TitleRow(context, t)) }
            }
        }
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}

/** Compact horizontal title row (mirror of TitleRow). */
class TitleRow(ctx: Context, val title: Title) : LinearLayout(ctx) {
    init {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = T.round(context, T.card(context), T.R_CARD)
        setPadding(T.dp(context, 10), T.dp(context, 10), T.dp(context, 10), T.dp(context, 10))
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = T.dp(context, T.GAP) }
        elevation = T.dp(context, 1).toFloat()
        isClickable = true
        setOnClickListener { DetailActivity.open(context, title) }
        addView(CoverView(context, title, 58, 80))
        addView(Ui.hspacer(context, 14))
        val col = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        col.addView(T.text(context, title.title, 15, T.foreground(context), bold = true).apply { maxLines = 2 })
        val meta = listOfNotNull(title.year?.toString(), title.totalUnits?.let { "$it ${if (title.medium == Medium.WATCH) "eps" else "caps"}" }, Fmt.formatScore(title.score)?.let { "★ $it" }).joinToString(" · ")
        col.addView(T.text(context, meta, 12, T.mutedFg(context)).apply { maxLines = 1 })
        addView(col)
        val inLib = AppState.doc.entries.containsKey(title.key)
        addView(T.icon(context, if (inLib) "e866" else "e5c8", 18, if (inLib) T.primary(context) else T.mutedFg(context)))
    }
}

/** Cover image or pastel placeholder. */
class CoverView(context: Context, title: Title, w: Int, h: Int) : FrameLayout(context) {
    init {
        background = T.round(context, T.tile(context, title.kind.tile).first, T.R_TILE)
        layoutParams = LinearLayout.LayoutParams(T.dp(context, w), T.dp(context, h))
        if (!title.cover.isNullOrBlank()) {
            addView(android.widget.ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                setImageURI(Uri.parse(title.cover))
            })
        } else {
            addView(T.icon(context, title.kind.icon, 28, T.tile(context, title.kind.tile).second), FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        }
    }
}

/** Simple text watcher helper. */
fun afterTextChanged(fn: (String?) -> Unit): android.text.TextWatcher = object : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { fn(s?.toString()) }
    override fun afterTextChanged(s: android.text.Editable?) {}
}
