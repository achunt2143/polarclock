package com.achunt.polarclock.palette

import androidx.core.graphics.ColorUtils

data class MaterialYouPalette(
    override val id: String,
    override val backgroundColor: Int,
    val secondary: Int,
    val tertiary: Int
) : ClockPalette(id, backgroundColor) {
    override fun getSecondColor(angle: Float) = secondary
    override fun getMinuteColor(angle: Float) = tertiary
    override fun getHourColor(angle: Float) = ColorUtils.blendARGB(backgroundColor, secondary, 0.3f)
    override fun getDayColor(angle: Float) = ColorUtils.blendARGB(secondary, tertiary, 0.5f)
    override fun getMonthColor(angle: Float) = ColorUtils.blendARGB(tertiary, backgroundColor, 0.3f)
}
