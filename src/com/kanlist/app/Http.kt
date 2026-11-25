package com.kanlist.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

/** Minimal HTTP + JSON helpers (HttpURLConnection, no external deps). */
object Http {
    data class Resp(val ok: Boolean, val code: Int, val body: String, val error: String? = null)

    fun get(url: String, headers: Map<String, String> = emptyMap(), timeoutMs: Int = 15000): Resp {
        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Kanlist/1.0 (Android)")
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            conn.disconnect()
            Resp(code in 200..299, code, body, if (code in 200..299) null else "HTTP $code")
        } catch (e: Exception) {
            Resp(false, -1, "", e.message ?: "network error")
        }
    }

    fun postJson(url: String, json: String, headers: Map<String, String> = emptyMap(), timeoutMs: Int = 15000): Resp {
        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                doOutput = true
                setRequestProperty("Content-Type", "application/json; utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Kanlist/1.0 (Android)")
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }
            conn.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            conn.disconnect()
            Resp(code in 200..299, code, body, if (code in 200..299) null else "HTTP $code")
        } catch (e: Exception) {
            Resp(false, -1, "", e.message ?: "network error")
        }
    }

    fun online(ctx: Context): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

/** Tiny JSON helpers over org.json. */
object J {
    fun obj(s: String): JSONObject? = try { JSONObject(s) } catch (e: Exception) { null }
    fun arr(s: String): JSONArray? = try { JSONArray(s) } catch (e: Exception) { null }

    fun str(o: JSONObject?, vararg path: String): String? {
        var cur: Any? = o
        for (p in path) {
            when (cur) {
                is JSONObject -> { if (!cur.has(p)) return null; cur = cur.get(p) }
                else -> return null
            }
        }
        return if (cur == null || cur == JSONObject.NULL) null else cur.toString()
    }

    fun int(o: JSONObject?, vararg path: String): Int? {
        val s = str(o, *path) ?: return null
        return s.toIntOrNull() ?: s.toDoubleOrNull()?.toInt()
    }

    fun bool(o: JSONObject?, vararg path: String): Boolean? {
        val s = str(o, *path) ?: return null
        return s == "true" || s == "1"
    }

    fun objAt(o: JSONObject?, vararg path: String): JSONObject? {
        var cur: Any? = o
        for (p in path) {
            when (cur) {
                is JSONObject -> { if (!cur.has(p)) return null; cur = cur.get(p) }
                else -> return null
            }
        }
        return cur as? JSONObject
    }

    fun arrAt(o: JSONObject?, vararg path: String): JSONArray? {
        var cur: Any? = o
        for (p in path) {
            when (cur) {
                is JSONObject -> { if (!cur.has(p)) return null; cur = cur.get(p) }
                else -> return null
            }
        }
        return cur as? JSONArray
    }

    fun strArray(a: JSONArray?): List<String> {
        if (a == null) return emptyList()
        return (0 until a.length()).mapNotNull { i -> a.opt(i)?.toString() }
    }
}
