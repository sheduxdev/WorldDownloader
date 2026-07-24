package dev.shedux.worlddownloader.ui

import dev.shedux.worlddownloader.render.Draw

object Components {

    fun hovered(x: Float, y: Float, w: Float, h: Float, mx: Int, my: Int): Boolean =
        mx >= x && mx <= x + w && my >= y && my <= y + h

    fun button(
        x: Float, y: Float, w: Float, h: Float,
        icon: String?, label: String,
        mx: Int, my: Int, primary: Boolean = false
    ): Boolean {
        val hov = hovered(x, y, w, h, mx, my)
        val bg = when {
            primary && hov -> 0xFFE4E4E7.toInt()
            primary -> Theme.TEXT
            hov -> Theme.BORDER
            else -> Theme.ELEVATED
        }
        val fg = if (primary) Theme.CARD else Theme.TEXT
        Draw.outline(x, y, w, h, 8f, 1f, Theme.BORDER, bg)

        val labelPx = 12f
        val iconPx = 12f
        val ty = y + h / 2f - labelPx / 2f - 1f
        var tx = x + 14f
        if (icon != null) {
            Theme.icons.draw(icon, tx, y + h / 2f - iconPx / 2f - 1f, fg, iconPx)
            tx += Theme.icons.width(icon, iconPx) + 8f
        }
        Theme.medium.draw(label, tx, ty, fg, labelPx)
        return hov
    }
}
