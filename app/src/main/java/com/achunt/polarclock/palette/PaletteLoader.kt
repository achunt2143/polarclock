package com.achunt.polarclock.palette

import android.content.Context
import android.util.Log
import com.achunt.polarclock.R
import org.xmlpull.v1.XmlPullParser

object PaletteLoader {
    fun loadPalettes(context: Context): MutableMap<String, ClockPalette> {
        val palettes = mutableMapOf<String, ClockPalette>()
        val parser = context.resources.getXml(R.xml.polar_clock_palettes)
        try {
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "palette") {
                    ClockPalette.parse(parser)?.let { palette ->
                        palette.id?.let { palettes[it] = palette }
                    }
                }
                event = parser.next()
            }
        } catch (t: Throwable) {
            Log.e("PaletteLoader", "Palette parsing error", t)
        } finally {
            parser.close()
        }
        return palettes
    }
}
