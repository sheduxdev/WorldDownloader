package dev.shedux.worlddownloader

import com.mojang.brigadier.CommandDispatcher
import dev.shedux.worlddownloader.ui.EmptyPanelScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object WorldDownloaderMod : ClientModInitializer {
    private lateinit var openKey: KeyBinding

    override fun onInitializeClient() {
        // /wdl command -> open empty panel
        ClientCommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<FabricClientCommandSource>, _ ->
            dispatcher.register(
                ClientCommandManager.literal("wdl").executes { ctx ->
                    ctx.source.client.send { ctx.source.client.setScreen(EmptyPanelScreen()) }
                    1
                }
            )
        }
        // rebindable hotkey (default: unbound), category worlddownloader
        openKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding("key.worlddownloader.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.worlddownloader")
        )
        ClientTickEvents.END_CLIENT_TICK.register { client: MinecraftClient ->
            while (openKey.wasPressed()) client.setScreen(EmptyPanelScreen())
        }
    }
}
