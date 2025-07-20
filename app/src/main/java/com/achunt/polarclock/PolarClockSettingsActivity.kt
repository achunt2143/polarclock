package com.achunt.polarclock

import android.app.WallpaperManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.achunt.polarclock.PolarClockWallpaper.Companion.SHARED_PREFS_NAME
import com.google.android.material.color.DynamicColors

class PolarClockSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        DynamicColors.applyToActivitiesIfAvailable(application)
        val wm = WallpaperManager.getInstance(applicationContext)
        wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.let { colors ->
            Log.d("PolarClockSettingsActivity", "Background color: ${colors.primaryColor.toArgb()}")
            editor.putInt("mat_bg", colors.primaryColor.toArgb() ?: PolarClockWallpaper.bg)
            editor.putInt("mat_sec", colors.secondaryColor?.toArgb() ?: PolarClockWallpaper.sec)
            editor.putInt("mat_tert", colors.tertiaryColor?.toArgb() ?: PolarClockWallpaper.tert)
            editor.apply()
        }
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PolarClockSettingsFragment())
            .commit()
    }
}
