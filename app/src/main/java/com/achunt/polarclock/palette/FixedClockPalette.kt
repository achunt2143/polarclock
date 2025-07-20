package com.achunt.polarclock.palette

import android.graphics.Color
import org.xmlpull.v1.XmlPullParser

data class FixedClockPalette(
    override val id: String?,
    override val backgroundColor: Int,
    val secondColor: Int,
    val minuteColor: Int,
    val hourColor: Int,
    val dayColor: Int,
    val monthColor: Int
) : ClockPalette(id, backgroundColor) {

    override fun getSecondColor(angle: Float) = secondColor
    override fun getMinuteColor(angle: Float) = minuteColor
    override fun getHourColor(angle: Float) = hourColor
    override fun getDayColor(angle: Float) = dayColor
    override fun getMonthColor(angle: Float) = monthColor

    companion object {
        fun parse(id: String?, xml: XmlPullParser): FixedClockPalette? {
            fun getColor(attr: String, fallback: String = "#000000") =
                Color.parseColor(xml.getAttributeValue(null, attr) ?: fallback)
            return FixedClockPalette(
                id = id,
                backgroundColor = getColor("background", "#FFFFFF"),
                secondColor = getColor("second"),
                minuteColor = getColor("minute"),
                hourColor = getColor("hour"),
                dayColor = getColor("day"),
                monthColor = getColor("month")
            )
        }
    }
}
