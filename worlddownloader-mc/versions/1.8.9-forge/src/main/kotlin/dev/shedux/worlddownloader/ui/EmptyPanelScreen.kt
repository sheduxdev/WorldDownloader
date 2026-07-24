package dev.shedux.worlddownloader.ui

import dev.shedux.worlddownloader.render.Draw
import net.minecraft.client.gui.GuiScreen

class EmptyPanelScreen : GuiScreen() {

    override fun doesGuiPauseGame() = false

    override fun initGui() {
        Theme.init()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        Draw.rect(0f, 0f, w, h, Theme.BG)

        val cw = 384f
        val ch = 300f
        val cx = (w - cw) / 2f
        val cy = (h - ch) / 2f
        val p = 24f
        val cxc = cx + cw / 2f
        Draw.outline(cx, cy, cw, ch, 16f, 1f, Theme.BORDER, Theme.CARD)

        val iconPx = 28f
        Theme.icons.draw(Theme.ICON_GLOBE, cxc - Theme.icons.width(Theme.ICON_GLOBE, iconPx) / 2f, cy + 28f, Theme.TEXT, iconPx)

        val title = "WorldDownloader"
        Theme.semibold.draw(title, cxc - Theme.semibold.width(title, 18f) / 2f, cy + 74f, Theme.TEXT, 18f)

        val sub = "Download server areas locally"
        Theme.regular.draw(sub, cxc - Theme.regular.width(sub, 11f) / 2f, cy + 100f, Theme.MUTED, 11f)

        Draw.rect(cx + p, cy + 128f, cw - p * 2f, 1f, Theme.BORDER)

        val bx = cx + p
        val bw = cw - p * 2f
        val bh = 34f
        Components.button(bx, cy + 144f, bw, bh, Theme.ICON_GLOBE, "Save as World", mouseX, mouseY, primary = true)
        Components.button(bx, cy + 186f, bw, bh, Theme.ICON_CUBE, "Save as Schematic", mouseX, mouseY)
        Components.button(bx, cy + 228f, bw, bh, Theme.ICON_CUBE, "Save as Litematic", mouseX, mouseY)

        val hint = "Press ESC to close"
        Theme.regular.draw(hint, cxc - Theme.regular.width(hint, 10f) / 2f, cy + ch - 24f, Theme.INPUT, 10f)
    }
}
