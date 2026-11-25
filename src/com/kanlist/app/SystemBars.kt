package com.kanlist.app

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager

/** Edge-to-edge + transparent system bars, like the Expo SafeAreaProvider setup. */
object SystemBars {
    fun apply(activity: Activity) {
        val w = activity.window
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        w.statusBarColor = Color.TRANSPARENT
        w.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
        }
        val dark = isDark(activity)
        val controller = w.decorView.windowInsetsController ?: return
        controller.setSystemBarsAppearance(
            if (dark) 0 else android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
            android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
        )
    }

    fun isDark(ctx: android.content.Context): Boolean {
        val mode = ctx.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    fun topInset(view: View): Int {
        val insets = view.rootWindowInsets ?: return 0
        return insets.systemWindowInsetTop
    }

    fun bottomInset(view: View): Int {
        val insets = view.rootWindowInsets ?: return 0
        return insets.systemWindowInsetBottom
    }
}
