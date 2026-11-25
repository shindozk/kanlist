package com.kanlist.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.Executors

/** Explore screen (mirror of app/(tabs)/explore.tsx): rails + genres. */
class ExploreScreen(ctx: Context) : ScrollView(ctx) {
    private val io = Executors.newSingleThreadExecutor()
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    private val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }

    init {
        setBackgroundColor(T.background(context))
        isVerticalScrollBarEnabled = false
        content.setPadding(0, T.dp(context, 18), 0, T.dp(context, 100))
        addView(content)
        build()
    }

    private fun build() {
        content.removeAllViews()
        content.addView(T.text(context, "Explorar", 24, T.foreground(context), bold = true).apply { setPadding(T.dp(context, T.SCREEN), 0, 0, 0) })
        content.addView(spacer(T.GAP))
        content.addView(T.text(context, "Descubra o que assistir e ler a seguir.", 13, T.mutedFg(context)).apply { setPadding(T.dp(context, T.SCREEN), 0, 0, 0) })
        content.addView(spacer(T.SECTION))
        for (preset in Catalog.presets()) {
            content.addView(railHeader(preset))
            content.addView(rail(preset))
            content.addView(spacer(T.SECTION))
        }
        content.addView(genreSection())
    }

    private fun railHeader(p: Catalog.Preset): View {
        val row = Ui.horizontal(context).apply { setPadding(T.dp(context, T.SCREEN), 0, T.dp(context, T.SCREEN), 0) }
        val t = T.text(context, p.label, 19, T.foreground(context), bold = true)
        t.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(t)
        row.addView(T.text(context, "Ver tudo", 13, T.foreground(context), bold = true).apply {
            setOnClickListener { BrowseActivity.open(context, p.id) }
        })
        return row
    }

    private fun rail(p: Catalog.Preset): View {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(T.dp(context, T.SCREEN), 0, T.dp(context, T.SCREEN), 0)
        }
        row.addView(T.text(context, "Carregando…", 13, T.mutedFg(context)).apply { setPadding(0, T.dp(context, 8), 0, 0) })
        io.execute {
            val items = Catalog.browse(p.id, 1)
            main.post {
                row.removeAllViews()
                if (items.isEmpty()) {
                    row.addView(T.text(context, "Nada por aqui", 13, T.mutedFg(context)))
                    return@post
                }
                for (t in items.take(12)) {
                    val card = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(T.dp(context, 124), LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = T.dp(context, 14) }; isClickable = true; setOnClickListener { DetailActivity.open(context, t) } }
                    card.addView(CoverView(context, t, 124, (124 * 1.42).toInt()))
                    card.addView(T.text(context, t.title, 13, T.foreground(context), bold = true).apply { maxLines = 2; setPadding(0, T.dp(context, 8), 0, 0) })
                    row.addView(card)
                }
            }
        }
        val scroll = android.widget.HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        scroll.addView(row)
        return scroll
    }

    private fun genreSection(): View {
        val v = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(T.dp(context, T.SCREEN), 0, T.dp(context, T.SCREEN), 0) }
        v.addView(Ui.sectionHeader(context, "Por gênero"))
        val genres = listOf("Ação", "Romance", "Fantasia", "Comédia", "Drama", "Mistério", "Terror", "Esportes", "Ficção científica")
        val row = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        var current = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        for (g in genres) {
            val cell = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                background = T.round(context, T.card(context), T.R_TILE)
                setPadding(T.dp(context, 12), T.dp(context, 14), T.dp(context, 12), T.dp(context, 14))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = T.dp(context, 10); bottomMargin = T.dp(context, 10) }
                isClickable = true
                setOnClickListener { BrowseActivity.open(context, "genre:${g}") }
            }
            cell.addView(View(context).apply { background = T.round(context, T.primary(context), 3); layoutParams = LinearLayout.LayoutParams(T.dp(context, 22), T.dp(context, 6)) })
            cell.addView(T.text(context, g, 13, T.foreground(context), bold = true).apply { maxLines = 1 })
            current.addView(cell)
            if (current.childCount >= 3) { row.addView(current); current = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL } }
        }
        if (current.childCount > 0) row.addView(current)
        v.addView(row)
        return v
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}

/** Browse grid (mirror of app/browse/[id].tsx). */
class BrowseActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBars.apply(this)
        val root = FrameLayout(this).apply { setBackgroundColor(T.background(this@BrowseActivity)) }
        setContentView(root)
        root.addView(BrowseScreen(this, intent.getStringExtra("id") ?: "") { finish() })
    }
    companion object { fun open(c: Context, id: String) { (c as? Activity)?.startActivity(android.content.Intent(c, BrowseActivity::class.java).putExtra("id", id)) } }
}

class BrowseScreen(ctx: Context, private val presetId: String, private val onBack: () -> Unit) : LinearLayout(ctx) {
    private val io = Executors.newSingleThreadExecutor()
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    private val grid = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(T.background(context))
        val top = Ui.horizontal(context).apply { setPadding(T.dp(context, T.SCREEN), T.dp(context, 10), T.dp(context, T.SCREEN), T.dp(context, 10)) }
        top.addView(Ui.iconButton(context, "e5c4", onClick = onBack))
        top.addView(T.text(context, Catalog.presetById(presetId)?.label ?: "Lista", 16, T.foreground(context), bold = true).apply { gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
        addView(top)
        val scroll = ScrollView(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f) }
        grid.setPadding(T.dp(context, T.SCREEN), 0, T.dp(context, T.SCREEN), T.dp(context, 40))
        scroll.addView(grid)
        addView(scroll)
        grid.addView(T.text(context, "Carregando…", 13, T.mutedFg(context)))
        io.execute {
            val items = Catalog.browse(presetId, 1)
            main.post {
                grid.removeAllViews()
                if (items.isEmpty()) { grid.addView(Ui.emptyState(context, "e2c1", "Nada por aqui")); return@post }
                var row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                for ((i, t) in items.withIndex()) {
                    val cell = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = T.dp(context, 12); bottomMargin = T.dp(context, 16) }; isClickable = true; setOnClickListener { DetailActivity.open(context, t) } }
                    cell.addView(CoverView(context, t, 0, 0).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, 170)) })
                    cell.addView(T.text(context, t.title, 13, T.foreground(context), bold = true).apply { maxLines = 2; setPadding(0, T.dp(context, 6), 0, 0) })
                    row.addView(cell)
                    if (row.childCount >= 3) { grid.addView(row); row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL } }
                }
                if (row.childCount > 0) grid.addView(row)
            }
        }
    }
}
