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

/** Profile / settings screen (mirror of app/(tabs)/profile.tsx). */
class ProfileScreen(ctx: Context) : ScrollView(ctx) {
    private val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }

    init {
        setBackgroundColor(T.background(context))
        isVerticalScrollBarEnabled = false
        content.setPadding(T.dp(context, T.SCREEN), T.dp(context, 18), T.dp(context, T.SCREEN), T.dp(context, 100))
        addView(content)
        rebuild()
    }

    private fun rebuild() {
        content.removeAllViews()
        val s = AppState.doc.settings
        content.addView(T.text(context, "Perfil", 24, T.foreground(context), bold = true))
        content.addView(spacer(T.GAP))
        // header card
        val head = Ui.card(context, pad = 16)
        val row = Ui.horizontal(context)
        val avatar = FrameLayout(context).apply { background = T.round(context, T.hero(context), 28); layoutParams = LinearLayout.LayoutParams(T.dp(context, 56), T.dp(context, 56)) }
        avatar.addView(T.icon(context, "e7fd", 26, T.brown(context)), FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        row.addView(avatar)
        row.addView(Ui.hspacer(context, 14))
        val col = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        col.addView(T.text(context, s.name.ifBlank { "Você" }, 18, T.foreground(context), bold = true))
        val stats = AppState.doc.entries.size
        col.addView(T.text(context, "$stats obras na biblioteca", 13, T.mutedFg(context)))
        row.addView(col)
        head.addView(row)
        content.addView(head)
        content.addView(spacer(T.SECTION))

        content.addView(Ui.sectionHeader(context, "Acompanhamento"))
        content.addView(settingRow("e8b5", "Meta diária", "${s.dailyGoal} por dia"))
        content.addView(settingRow("e894", "Idioma dos títulos", if (s.titleLanguage == "english") "Inglês" else if (s.titleLanguage == "native") "Nativo" else "Romaji"))
        content.addView(settingRow("e8e2", "Traduzir sinopse", if (s.autoTranslate) "Ativado" else "Desativado"))
        content.addView(spacer(T.SECTION))

        content.addView(Ui.sectionHeader(context, "Aparência"))
        content.addView(themeRow())
        content.addView(spacer(T.SECTION))

        content.addView(Ui.sectionHeader(context, "Dados"))
        content.addView(settingRow("e2c4", "Exportar backup", "JSON da biblioteca"))
        content.addView(settingRow("e2c6", "Importar backup", "Mesclar ou substituir"))
        content.addView(settingRow("e92b", "Apagar tudo", "Remove biblioteca e histórico"))
        content.addView(spacer(T.SECTION))

        content.addView(T.text(context, "Kanlist · Material You", 12, T.mutedFg(context)).apply { gravity = Gravity.CENTER })
    }

    private fun themeRow(): View {
        val row = Ui.horizontal(context)
        row.addView(Ui.chip(context, "Sistema", AppState.doc.settings.theme == "system") { AppState.doc.settings.theme = "system"; AppState.save(context); rebuild() })
        row.addView(Ui.hspacer(context, 8))
        row.addView(Ui.chip(context, "Claro", AppState.doc.settings.theme == "light") { AppState.doc.settings.theme = "light"; AppState.save(context); rebuild() })
        row.addView(Ui.hspacer(context, 8))
        row.addView(Ui.chip(context, "Escuro", AppState.doc.settings.theme == "dark") { AppState.doc.settings.theme = "dark"; AppState.save(context); rebuild() })
        return row
    }

    private fun settingRow(icon: String, label: String, hint: String? = null): View {
        val row = Ui.horizontal(context).apply {
            setPadding(0, T.dp(context, 10), 0, T.dp(context, 10))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        row.addView(Ui.iconTile(context, icon, "peach", 38, 12))
        row.addView(Ui.hspacer(context, 14))
        val col = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        col.addView(T.text(context, label, 15, T.foreground(context), bold = true))
        if (hint != null) col.addView(T.text(context, hint, 12, T.mutedFg(context)))
        row.addView(col)
        row.addView(T.icon(context, "e5c8", 18, T.mutedFg(context)))
        return row
    }

    private fun spacer(h: Int): View = View(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, T.dp(context, h)) }
}
