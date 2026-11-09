package com.kanlist

import android.app.Application
import androidx.room.Room
import com.kanlist.data.local.KanlistDatabase
import com.kanlist.data.source.local.preferences.PreferencesDataSource
import dagger.hilt.android.HiltAndroidApp
import kotlin.jvm.JvmStatic

@HiltAndroidApp
class KanlistApplication : Application() {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        var instance: KanlistApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize database
        KanlistDatabase.getInstance(this)
        // Initialize preferences
        PreferencesDataSource.getInstance(this)
    }
}