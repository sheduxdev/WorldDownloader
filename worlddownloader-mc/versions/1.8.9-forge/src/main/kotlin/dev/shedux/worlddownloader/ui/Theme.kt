package dev.shedux.worlddownloader.ui

import dev.shedux.worlddownloader.render.GLFont

object Theme {
    // shadcn zinc, pure-black surface
    const val BG = 0xFF000000.toInt()
    const val CARD = 0xFF09090B.toInt()       // zinc-950
    const val ELEVATED = 0xFF18181B.toInt()   // zinc-900
    const val BORDER = 0xFF27272A.toInt()     // zinc-800
    const val INPUT = 0xFF3F3F46.toInt()      // zinc-700
    const val TEXT = 0xFFFAFAFA.toInt()       // zinc-50
    const val MUTED = 0xFFA1A1AA.toInt()      // zinc-400
    const val OVERLAY = 0xCC000000.toInt()

    // Font Awesome 6 Free (solid) codepoints, built from ints to avoid source-encoding issues
    private fun icon(cp: Int) = String(Character.toChars(cp))
    val ICON_GLOBE = icon(0xF0AC)
    val ICON_DOWNLOAD = icon(0xF019)
    val ICON_CUBE = icon(0xF1B2)

    lateinit var regular: GLFont
    lateinit var medium: GLFont
    lateinit var semibold: GLFont
    lateinit var icons: GLFont

    private const val DIR = "/assets/worlddownloader/fonts"

    fun init() {
        if (::regular.isInitialized) return
        regular = GLFont("$DIR/Sen-Regular.ttf", 36f)
        medium = GLFont("$DIR/Sen-Medium.ttf", 36f)
        semibold = GLFont("$DIR/Sen-SemiBold.ttf", 36f)
        icons = GLFont("$DIR/fa-solid-900.ttf", 36f)
    }
}
