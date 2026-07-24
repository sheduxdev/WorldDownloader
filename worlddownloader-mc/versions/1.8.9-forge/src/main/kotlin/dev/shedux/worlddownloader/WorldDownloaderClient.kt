package dev.shedux.worlddownloader

import dev.shedux.worlddownloader.ui.EmptyPanelScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

object WorldDownloaderClient {
    private val openKey = KeyBinding("key.worlddownloader.open", Keyboard.KEY_BACKSLASH, "category.worlddownloader")
    private var pendingOpen = false

    fun init() {
        ClientCommandHandler.instance.registerCommand(object : CommandBase() {
            override fun getCommandName() = "wdl"
            override fun getCommandUsage(sender: ICommandSender) = "/wdl"
            override fun getRequiredPermissionLevel() = 0
            override fun processCommand(sender: ICommandSender, args: Array<out String>) { pendingOpen = true }
        })
        ClientRegistry.registerKeyBinding(openKey)
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (e.phase != TickEvent.Phase.END) return
        if (openKey.isPressed) pendingOpen = true
        if (pendingOpen) {
            pendingOpen = false
            // open here, not in the command: chat closes with displayGuiScreen(null) right after the command runs
            Minecraft.getMinecraft().displayGuiScreen(EmptyPanelScreen())
        }
    }
}
