package com.kanlist.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

/** Title detail — mirrors app/title/[key].tsx and features/title. */
class DetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBars.apply(this)
        AppState.hydrate(this)
        val root = FrameLayout(this).apply { setBackgroundColor(T.background(this@DetailActivity)) }
        setContentView(root)
        val key = intent.getStringExtra("key") ?: ""
        val title = intent.getSerializableExtra("title") as? Title
        root.addView(DetailScreen(this, key, title) { finish() })
    }

    companion object {
        fun open(c: Context, t: Title) {
            (c as? Activity)?.startActivity(Intent(c, DetailActivity::class.java).apply {
                putExtra("key", t.key)
                putExtra("title", t)
            })
        }
    }
}

class DetailScreen(ctx: Context, private val key: String, private val initial: Title?, private val onBack: () -> Unit) : LinearLayout(ctx) {
    private val io = Executors.newSingleThreadExecutor()
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    private val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private var title: Title? = initial

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(T.background(context))
        val top = Ui.horizontal(context).apply { setPadding(T.dp(context, T.SCREEN), T.dp(context, 10), T.dp(context, T.SCREEN), T.dp(context, 10)) }
        top.addView(Ui.iconButton(context, "e5c4", onClick = onBack))
        val spacer = View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) }
        top.addView(spacer)
        addView(top)
        val scroll = ScrollView(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f); isVerticalScrollBarEnabled = false }
        content.setPadding(T.dp(context, T.SCREEN), 0, T.dp(context, T.SCREEN), T.dp(context, 40))
        scroll.addView(content)
        addView(scroll)
        renderLoading()
        load()
    }

    private fun renderLoading() {
        content.removeAllViews()
        content.addView(T.text(context, "Carregando…", 15, T.mutedFg(context)))
    }

    private fun load() {
        io.execute {
            val t = title ?: Catalog.detail(key)
            main.post {
                if (t == null) {
                    content.removeAllViews()
                    content.addView(Ui.emptyState(context, "e2c1", "Não encontramos essa obra"))
                    return@post
                }
                title = t
                render(t)
            }
        }
    }

    private fun render(t: Title) {
        content.removeAllViews()
        // header: cover + info
        val header = Ui.horizontal(context).apply { gravity = Gravity.TOP }
        header.addView(CoverView(context, t, 120, 170))
        header.addView(Ui.hspacer(context, 16))
        val col = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        col.addView(T.text(context, t.title, 20, T.foreground(context), bold = true).apply { maxLines = 3 })
        if (t.titleAlt != null) col.addView(T.text(context, t.titleAlt!!, 13, T.mutedFg(context)).apply { maxLines = 2 })
        col.addView(spacer(8))
        val kindBadge = T.text(context, t.kind.label, 11, T.tile(context, t.kind.tile).second, bold = true).apply {
            background = T.round(context, T.tile(context, t.kind.tile).first, T.R_PILL)
            setPadding(T.dp(context, 8), T.dp(context, 4), T.dp(context, 8), T.dp(context, 4))
        }
        col.addView(kindBadge)
        col.addView(spacer(8))
        val meta = listOfNotNull(t.year?.toString(), Fmt.releaseLabel(t.status), Fmt.formatScore(t.score)?.let { "★ $it" }, t.totalUnits?.let { "${Fmt.unitWord(t.medium, true)}: $it" }).joinToString(" · ")
        col.addView(T.text(context, meta, 12, T.mutedFg(context)).apply { maxLines = 2 })
        header.addView(col)
        content.addView(header)
        content.addView(spacer(T.SECTION))

        // action buttons
        val entry = AppState.entry(t.key)
        val actions = Ui.horizontal(context)
        if (entry == null) {
            actions.addView(Ui.pill(context, "Adicionar", icon = "e145") {
                AppState.add(t, TrackingStatus.PLANNING); AppState.save(context); render(t)
            })
        } else {
            actions.addView(Ui.pill(context, "Marcar próximo", icon = "e5ca") {
                AppState.markNext(t.key); AppState.save(context); render(t)
            })
            actions.addView(Ui.hspacer(context, 8))
            actions.addView(Ui.pill(context, "Remover", variant = "outline") {
                AppState.remove(t.key); AppState.save(context); render(t)
            })
        }
        content.addView(actions)
        content.addView(spacer(T.SECTION))

        // progress
        if (entry != null) {
            val total = AppState.totalFor(entry)
            val done = AppState.doneCount(entry)
            content.addView(T.text(context, "Progresso · $done${total?.let { "/$it" } ?: ""}", 15, T.foreground(context), bold = true))
            content.addView(spacer(8))
            if (total != null) content.addView(Ui.progressBar(context, done.toFloat() / total))
            content.addView(spacer(T.SECTION))
        }

        // synopsis
        if (!t.synopsis.isNullOrBlank()) {
            content.addView(Ui.sectionHeader(context, "Sinopse"))
            val card = Ui.card(context, pad = 16)
            card.addView(T.text(context, t.synopsis!!, 14, T.foreground(context)).apply { maxLines = 8 })
            content.addView(card)
            content.addView(spacer(T.SECTION))
        }

        // genres
        if (t.genres.isNotEmpty()) {
            content.addView(Ui.sectionHeader(context, "Gêneros"))
            val row = Ui.horizontal(context).apply { }
            t.genres.take(6).forEach { row.addView(Ui.chip(context, it)); row.addView(Ui.hspacer(context, 8)) }
            content.addView(row)
        }
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}
