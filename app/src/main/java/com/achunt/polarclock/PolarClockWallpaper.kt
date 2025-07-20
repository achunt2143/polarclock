package com.achunt.polarclock

import android.content.*
import android.graphics.*
import android.os.*
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.achunt.polarclock.palette.*
import java.time.ZonedDateTime
import java.time.ZoneId
import kotlin.math.*

class PolarClockWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = ClockEngine()

    inner class ClockEngine : Engine(), SharedPreferences.OnSharedPreferenceChangeListener {
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        private val rect = RectF()
        private var prefs: SharedPreferences? = null
        private var visible = false
        private val palettes = mutableMapOf<String, ClockPalette>()
        private var showSeconds = true
        private var variableLineWidth = true
        private var activePalette: ClockPalette? = null
        private var paletteId: String? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            palettes.putAll(PaletteLoader.loadPalettes(applicationContext))
            palettes[MATERIAL_YOU_ID] = getMaterialYouPalette()
            prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            prefs?.registerOnSharedPreferenceChangeListener(this)
            applyPreferences()
            drawFrame()
            super.onCreate(surfaceHolder)
        }


        private fun getMaterialYouPalette(): ClockPalette {
            val storedBg = prefs?.getInt("mat_bg", bg)
            val storedSec = prefs?.getInt("mat_sec", sec)
            val storedTert = prefs?.getInt("mat_tert", tert)
            return MaterialYouPalette(
                id = MATERIAL_YOU_ID,
                backgroundColor = storedBg ?: bg,
                secondary = storedSec ?: sec,
                tertiary = storedTert ?: tert
            )
        }

        override fun onDestroy() {
            handler.removeCallbacks(drawRunner)
            prefs?.unregisterOnSharedPreferenceChangeListener(this)
            super.onDestroy()
        }


        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) drawFrame()
            else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            applyPreferences()
            drawFrame()
        }

        private fun applyPreferences() {
            val p = prefs ?: return
            showSeconds = p.getBoolean(PREF_SHOW_SECONDS, true)
            variableLineWidth = p.getBoolean(PREF_VARIABLE_LINE_WIDTH, true)
            val newPaletteId = p.getString(PREF_PALETTE, DEFAULT_PALETTE_ID)
            if (newPaletteId == MATERIAL_YOU_ID) {
                palettes[MATERIAL_YOU_ID] = getMaterialYouPalette()
            }
            paletteId = newPaletteId
            activePalette = palettes[paletteId] ?: palettes[DEFAULT_PALETTE_ID] ?: palettes.values.firstOrNull()
        }

        private val drawRunner = Runnable { drawFrame() }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            val frame = holder.surfaceFrame
            val width = frame.width()
            val height = frame.height()
            var canvas: Canvas? = null
            val palette = activePalette ?: return

            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val now = ZonedDateTime.now(ZoneId.systemDefault())
                    canvas.drawColor(palette.backgroundColor)
                    canvas.translate(width / 2f, height / 2f)
                    canvas.rotate(-90f)
                    var size = (min(width, height) * 0.5f - DEFAULT_RING_THICKNESS)
                    if (showSeconds) {
                        val secAngle = now.second / 60f + now.nano / 1e9f / 60f
                        paint.color = palette.getSecondColor(secAngle)
                        paint.strokeWidth = if (variableLineWidth) SMALL_RING_THICKNESS else DEFAULT_RING_THICKNESS
                        rect.set(-size, -size, size, size)
                        canvas.drawArc(rect, 0f, secAngle * 360f, false, paint)
                        size -= (SMALL_GAP + paint.strokeWidth)
                    }
                    val minAngle = (now.minute + now.second / 60f) / 60f
                    paint.color = palette.getMinuteColor(minAngle)
                    paint.strokeWidth = if (variableLineWidth) MEDIUM_RING_THICKNESS else DEFAULT_RING_THICKNESS
                    rect.set(-size, -size, size, size)
                    canvas.drawArc(rect, 0f, minAngle * 360f, false, paint)
                    size -= (SMALL_GAP + paint.strokeWidth)
                    val hourAngle = (now.hour + now.minute / 60f) / 24f
                    paint.color = palette.getHourColor(hourAngle)
                    paint.strokeWidth = if (variableLineWidth) LARGE_RING_THICKNESS else DEFAULT_RING_THICKNESS
                    rect.set(-size, -size, size, size)
                    canvas.drawArc(rect, 0f, hourAngle * 360f, false, paint)
                    size -= (LARGE_GAP + paint.strokeWidth)
                    val maxDay = now.month.length(now.toLocalDate().isLeapYear)
                    val dayAngle = ((now.dayOfMonth - 1).toFloat() / (maxDay - 1)).coerceAtLeast(0f)
                    paint.color = palette.getDayColor(dayAngle)
                    paint.strokeWidth = if (variableLineWidth) MEDIUM_RING_THICKNESS else DEFAULT_RING_THICKNESS
                    rect.set(-size, -size, size, size)
                    canvas.drawArc(rect, 0f, dayAngle * 360f, false, paint)
                    size -= (SMALL_GAP + paint.strokeWidth)
                    val monthAngle = (now.monthValue - 1) / 11f
                    paint.color = palette.getMonthColor(monthAngle)
                    paint.strokeWidth = if (variableLineWidth) LARGE_RING_THICKNESS else DEFAULT_RING_THICKNESS
                    rect.set(-size, -size, size, size)
                    canvas.drawArc(rect, 0f, monthAngle * 360f, false, paint)
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Draw error", e)
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
            handler.removeCallbacks(drawRunner)
            if (visible) {
                handler.postDelayed(drawRunner, if (showSeconds) FRAME_DELAY_MS else 2000L)
            }
        }
    }

    companion object {
        private const val LOG_TAG = "PolarClockModern"
        const val SHARED_PREFS_NAME = "polar_clock_settings"
        const val PREF_SHOW_SECONDS = "show_seconds"
        const val PREF_VARIABLE_LINE_WIDTH = "variable_line_width"
        const val PREF_PALETTE = "palette"
        const val DEFAULT_PALETTE_ID = "default"
        const val MATERIAL_YOU_ID = "material_you"
        private const val SMALL_RING_THICKNESS = 8f
        private const val MEDIUM_RING_THICKNESS = 16f
        private const val LARGE_RING_THICKNESS = 32f
        private const val DEFAULT_RING_THICKNESS = 24f
        private const val SMALL_GAP = 14f
        private const val LARGE_GAP = 38f
        private const val FRAME_DELAY_MS = 1000L / 25L
        val bg = Color.WHITE
        val sec = Color.parseColor("#0099CC")
        val tert = Color.parseColor("#669900")
    }
}
