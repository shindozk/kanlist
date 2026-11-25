package com.kanlist.app

import android.content.Context
import java.util.concurrent.Executors

/** App-wide state + library operations (mirror of state/library.ts) backed by Store. */
object AppState {
    private val io = Executors.newSingleThreadExecutor()
    private val main = android.os.Handler(android.os.Looper.getMainLooper())

    var doc: Store.Doc = Store.Doc()
        private set
    var ready = false
        private set

    fun hydrate(ctx: Context) {
        if (ready) return
        doc = Store.load(ctx)
        ready = true
    }

    fun save(ctx: Context) {
        io.execute { Store.save(ctx, doc) }
    }

    private fun now() = System.currentTimeMillis()

    fun entry(key: String): LibraryEntry? = doc.entries[key]

    fun add(title: Title, status: TrackingStatus = TrackingStatus.PLANNING) {
        val cur = doc.entries[title.key]
        if (cur != null) {
            cur.status = status
            cur.updatedAt = now()
        } else {
            val t = now()
            doc.entries[title.key] = LibraryEntry(
                title.key, title, status,
                startedAt = if (status == TrackingStatus.CURRENT) t else null,
                addedAt = t, updatedAt = t,
            )
        }
    }

    fun remove(key: String) {
        doc.entries.remove(key)
        doc.events.removeAll { it.key == key }
        doc.collections.forEach { it.keys.remove(key) }
    }

    fun setStatus(key: String, status: TrackingStatus) {
        val e = doc.entries[key] ?: return
        val t = now()
        e.status = status
        e.updatedAt = t
        if (status == TrackingStatus.CURRENT && e.startedAt == null) e.startedAt = t
        if (status == TrackingStatus.COMPLETED) {
            e.finishedAt = t
            val total = totalFor(e)
            if (total != null && e.done.size < total) for (i in 1..total) e.done.getOrPut(i.toString()) { t }
        }
        if (status == TrackingStatus.REPEATING && e.status == TrackingStatus.COMPLETED) {
            e.repeatCount++
            e.done.clear()
        }
    }

    fun toggleUnit(key: String, unit: String) {
        val e = doc.entries[key] ?: return
        if (e.done.containsKey(unit)) {
            e.done.remove(unit)
            val idx = doc.events.indexOfLast { it.key == key && it.unit == unit && it.count == 1 }
            if (idx >= 0) doc.events.removeAt(idx)
        } else {
            e.done[unit] = now()
            doc.events.add(newEvent(e, unit, 1))
        }
        e.updatedAt = now()
    }

    fun markMany(key: String, keys: List<String>) {
        val e = doc.entries[key] ?: return
        val added = keys.filter { !e.done.containsKey(it) }
        if (added.isEmpty()) return
        val t = now()
        added.forEach { e.done[it] = t }
        doc.events.add(newEvent(e, added.first(), added.size))
        e.updatedAt = t
    }

    fun unmarkMany(key: String, keys: List<String>) {
        val e = doc.entries[key] ?: return
        keys.forEach { e.done.remove(it) }
        e.updatedAt = now()
    }

    fun markNext(key: String): String? {
        val e = doc.entries[key] ?: return null
        var n = 1
        while (e.done.containsKey(n.toString())) n++
        val total = totalFor(e)
        if (total != null && n > total) return null
        toggleUnit(key, n.toString())
        return n.toString()
    }

    fun resetProgress(key: String) {
        doc.entries[key]?.let { it.done.clear(); it.updatedAt = now() }
    }

    private fun newEvent(e: LibraryEntry, unit: String, count: Int) = ActivityEvent(
        id = uid(), key = e.key, kind = e.title.kind, unit = unit, count = count,
        at = now(), minutes = count * minutesPerUnit(e.title),
    )

    // ---- progress helpers ----
    fun totalFor(e: LibraryEntry): Int? = e.customTotal ?: e.title.totalUnits
    fun doneCount(e: LibraryEntry): Int = e.done.size
    fun progressOf(e: LibraryEntry): Float? {
        val total = totalFor(e) ?: return null
        return (doneCount(e).toFloat() / total).coerceIn(0f, 1f)
    }
    fun lastDone(e: LibraryEntry): Int = e.done.keys.mapNotNull { it.toIntOrNull() }.maxOrNull() ?: 0
    fun nextUnit(e: LibraryEntry): Int? {
        val total = totalFor(e)
        var n = 1
        while (e.done.containsKey(n.toString())) n++
        if (total != null && n > total) return null
        return n
    }
    fun minutesPerUnit(t: Title): Int = t.unitDuration ?: t.kind.minutes

    // ---- collections ----
    fun createCollection(name: String, tile: String): String {
        val id = uid()
        doc.collections.add(Collection(id, name, tile = tile))
        return id
    }

    fun toggleInCollection(id: String, key: String) {
        doc.collections.find { it.id == id }?.let { c ->
            if (c.keys.contains(key)) c.keys.remove(key) else c.keys.add(key)
            c.updatedAt = now()
        }
    }

    fun deleteCollection(id: String) { doc.collections.removeAll { it.id == id } }

    fun updateSettings(patch: (Settings) -> Unit) { patch(doc.settings) }

    fun addSearch(q: String) {
        val v = q.trim()
        if (v.length < 2) return
        doc.searches.removeAll { it.equals(v, true) }
        doc.searches.add(0, v)
        while (doc.searches.size > 10) doc.searches.removeAt(doc.searches.size - 1)
    }

    fun uid(): String = now().toString(36) + java.lang.Math.random().toString().substring(2, 8)
}

/** Formatting + date helpers (mirror of core/dates.ts + core/text.ts). */
object Fmt {
    val WEEKDAYS_SHORT = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
    val WEEKDAYS_LONG = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
    val MONTHS = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")

    fun greeting(d: java.util.Calendar = java.util.Calendar.getInstance()): String {
        val h = d.get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            h < 5 -> "Boa noite"
            h < 12 -> "Bom dia"
            h < 18 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    fun longDate(c: java.util.Calendar): String =
        "${WEEKDAYS_LONG[(c.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7]}, ${c.get(java.util.Calendar.DAY_OF_MONTH)} de ${MONTHS[c.get(java.util.Calendar.MONTH)]} de ${c.get(java.util.Calendar.YEAR)}"

    fun dayKey(ts: Long): String {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = ts }
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    fun dayKey(c: java.util.Calendar): String =
        "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))

    fun startOfDay(ts: Long): Long {
        val c = java.util.Calendar.getInstance().apply {
            timeInMillis = ts
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun weekOf(ts: Long): List<Long> {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = startOfDay(ts); set(java.util.Calendar.HOUR_OF_DAY, 0) }
        val offset = (c.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
        c.add(java.util.Calendar.DAY_OF_MONTH, -offset)
        return (0..6).map { c.apply { add(java.util.Calendar.DAY_OF_MONTH, if (it == 0) 0 else 1) }.timeInMillis }
    }

    fun formatScore(score: Int?): String? = score?.let { "%.1f".format(it / 10.0).replace(".", ",") }

    fun formatMinutes(min: Int): String {
        if (min < 60) return "$min min"
        val h = min / 60; val m = min % 60
        if (h >= 48) return "${h / 24} dias"
        return if (m != 0) "${h}h ${m}min" else "${h}h"
    }

    fun timeHM(ts: Long): String {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = ts }
        return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
    }

    fun unitWord(m: Medium, plural: Boolean = false): String =
        if (m == Medium.WATCH) (if (plural) "episódios" else "episódio") else (if (plural) "capítulos" else "capítulo")

    fun unitShort(m: Medium): String = if (m == Medium.WATCH) "Ep" else "Cap"

    fun statusLabel(s: TrackingStatus, m: Medium): String {
        val w = m == Medium.WATCH
        return when (s) {
            TrackingStatus.CURRENT -> if (w) "Assistindo" else "Lendo"
            TrackingStatus.PLANNING -> if (w) "Quero assistir" else "Quero ler"
            TrackingStatus.COMPLETED -> "Completo"
            TrackingStatus.PAUSED -> "Pausado"
            TrackingStatus.DROPPED -> "Abandonado"
            TrackingStatus.REPEATING -> if (w) "Reassistindo" else "Relendo"
        }
    }

    fun releaseLabel(s: ReleaseStatus): String = when (s) {
        ReleaseStatus.RELEASING -> "Em lançamento"
        ReleaseStatus.FINISHED -> "Finalizado"
        ReleaseStatus.UPCOMING -> "Em breve"
        ReleaseStatus.HIATUS -> "Em hiato"
        ReleaseStatus.CANCELLED -> "Cancelado"
        ReleaseStatus.UNKNOWN -> "—"
    }

    fun titleName(t: Title, lang: String): String = when (lang) {
        "english" -> t.titleAlt ?: t.title
        "native" -> t.titleNative ?: t.title
        else -> t.title
    }
}
