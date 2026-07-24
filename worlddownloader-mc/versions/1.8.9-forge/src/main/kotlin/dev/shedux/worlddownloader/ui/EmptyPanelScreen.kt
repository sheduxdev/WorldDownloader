package dev.shedux.worlddownloader.ui

import net.minecraft.client.gui.GuiScreen

// A: intentionally empty shell. shadcn-zinc styling is sub-project D.
class EmptyPanelScreen : GuiScreen() {
    override fun doesGuiPauseGame(): Boolean = false
}
