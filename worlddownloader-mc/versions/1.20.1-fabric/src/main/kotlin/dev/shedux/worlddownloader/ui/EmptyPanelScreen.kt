package dev.shedux.worlddownloader.ui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

// A: intentionally empty shell. shadcn-zinc styling is sub-project D.
class EmptyPanelScreen : Screen(Text.literal("WorldDownloader")) {
    override fun shouldPause(): Boolean = false
}
