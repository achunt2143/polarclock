package com.achunt.polarclock.palette

import org.xmlpull.v1.XmlPullParser

sealed class ClockPalette(open val id: String?, open val backgroundColor: Int) {
    abstract fun getSecondColor(angle: Float): Int
    abstract fun getMinuteColor(angle: Float): Int
    abstract fun getHourColor(angle: Float): Int
    abstract fun getDayColor(angle: Float): Int
    abstract fun getMonthColor(angle: Float): Int

    companion object {
        fun parse(xml: XmlPullParser): ClockPalette? {
            val kind = xml.getAttributeValue(null, "kind") ?: "fixed"
            val id = xml.getAttributeValue(null, "id")
            return when (kind) {
                "cycling" -> CyclingClockPalette.parse(id, xml)
                else -> FixedClockPalette.parse(id, xml)
            }
        }
    }
}
