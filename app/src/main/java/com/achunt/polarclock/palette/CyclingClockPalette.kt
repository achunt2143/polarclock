package com.achunt.polarclock.palette

import android.graphics.Color
import org.xmlpull.v1.XmlPullParser

data class CyclingClockPalette(
    override val id: String?,
    override val backgroundColor: Int,
    val saturation: Float,
    val brightness: Float,
    val cache: IntArray = generateHSBCache(saturation, brightness)
) : ClockPalette(id, backgroundColor) {

    companion object {
        private const val PALETTE_STEPS = 360

        private fun hsbToColor(h: Float, s: Float, b: Float): Int {
            return Color.HSVToColor(floatArrayOf(h * 360f, s, b))
        }

        fun generateHSBCache(s: Float, b: Float): IntArray =
            IntArray(PALETTE_STEPS) { i -> hsbToColor(i / PALETTE_STEPS.toFloat(), s, b) }

        fun parse(id: String?, xml: XmlPullParser): CyclingClockPalette? {
            val bg = Color.parseColor(xml.getAttributeValue(null, "background") ?: "#FFFFFF")
            val sat = xml.getAttributeValue(null, "saturation")?.toFloatOrNull() ?: 1.0f
            val bri = xml.getAttributeValue(null, "brightness")?.toFloatOrNull() ?: 1.0f
            return CyclingClockPalette(id, bg, sat, bri)
        }
    }

    private fun colorAt(angle: Float) = cache[((angle % 1f) * cache.size).toInt() % cache.size]

    override fun getSecondColor(angle: Float) = colorAt(angle)
    override fun getMinuteColor(angle: Float) = colorAt(angle)
    override fun getHourColor(angle: Float) = colorAt(angle)
    override fun getDayColor(angle: Float) = colorAt(angle)
    override fun getMonthColor(angle: Float) = colorAt(angle)
}
