package dev.desco.worlddownloader.commands

import dev.desco.worlddownloader.core.WorldScreen
import dev.desco.worlddownloader.core.configs.impl.SchematicSettings
import dev.desco.worlddownloader.downloads.SchematicDownloader
import dev.desco.worlddownloader.utils.ChunkProviderCache
import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.SubCommand
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

object WorldDownloadCommand: Command("wdl") {

    @DefaultHandler
    fun handle() {
        EssentialAPI.getGuiUtil().openScreen(WorldScreen())
    }

    @SubCommand("save")
    fun save(subfolder: String, name: String) {
        val minecraft = Minecraft.getMinecraft()
        val world = minecraft.theWorld ?: return
        val provider = world.chunkProvider as? ChunkProviderCache ?: return
        provider.isSaving = true

        val settings = SchematicSettings().apply {
            outputFolder = subfolder
            outputName = name
            entities = false
        }

        val chunks = (provider.getCachedChunks() + provider.getChunks()).toSet()
        if (chunks.isEmpty()) {
            minecraft.thePlayer.addChatMessage(ChatComponentText(
                "${EnumChatFormatting.RED}No chunks are available to save."
            ))
            return
        }

        SchematicDownloader(settings).saveChunks(chunks)
    }
}
