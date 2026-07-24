package dev.shedux.worlddownloader.ui

import net.minecraft.client.gui.GuiScreen

class EmptyPanelScreen : GuiScreen() {
    override fun doesGuiPauseGame() = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRendererObj, "WorldDownloader", width / 2, height / 2 - 4, 0xFFFFFF)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}
