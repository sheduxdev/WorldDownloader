package dev.shedux.worlddownloader

import dev.shedux.worlddownloader.ui.EmptyPanelScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard

object WorldDownloaderClient {
    private val openKey = KeyBinding("key.worlddownloader.open", Keyboard.KEY_NONE, "category.worlddownloader")

    fun init() {
        // /wdl command -> open empty panel
        ClientCommandHandler.instance.registerCommand(object : CommandBase() {
            override fun getCommandName() = "wdl"
            override fun getCommandUsage(sender: ICommandSender) = "/wdl"
            override fun getRequiredPermissionLevel() = 0
            override fun processCommand(sender: ICommandSender, args: Array<out String>) {
                Minecraft.getMinecraft().addScheduledTask {
                    Minecraft.getMinecraft().displayGuiScreen(EmptyPanelScreen())
                }
            }
        })
        // rebindable hotkey (default: unbound), category worlddownloader
        ClientRegistry.registerKeyBinding(openKey)
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onKey(e: InputEvent.KeyInputEvent) {
        if (openKey.isPressed) {
            Minecraft.getMinecraft().displayGuiScreen(EmptyPanelScreen())
        }
    }
}
