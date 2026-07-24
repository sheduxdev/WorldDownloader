package dev.shedux.worlddownloader;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

// ponytail: tiny Java @Mod bootstrap avoids a Kotlin language-adapter dep (Forgelin); all logic is Kotlin.
@Mod(modid = "worlddownloader", name = "WorldDownloader", version = "0.0.1", clientSideOnly = true)
public class WorldDownloaderMod {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        WorldDownloaderClient.INSTANCE.init();
    }
}
