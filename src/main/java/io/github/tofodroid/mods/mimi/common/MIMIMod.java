package io.github.tofodroid.mods.mimi.common;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.gui.GuiWrapper;
import io.github.tofodroid.mods.mimi.common.loot.ModLootModifiers;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.server.ServerGuiWrapper;
import io.github.tofodroid.mods.mimi.server.ServerProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MIMIMod.MODID)
public class MIMIMod
{
    public static final String MODID = "mimi";

    public static final Logger LOGGER = LogManager.getLogger();
    public static Proxy proxy = (Proxy)DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static GuiWrapper guiWrapper = (GuiWrapper)DistExecutor.safeRunForDist(() -> ClientGuiWrapper::new, () -> ServerGuiWrapper::new);

    public MIMIMod() {
        MIMIMod.preInit(FMLJavaModLoadingContext.get(), ModLoadingContext.get());
    }

    public static void preInit(FMLJavaModLoadingContext fmlContext, ModLoadingContext modContext) {
        // Event Listener Registration
        fmlContext.getModEventBus().addListener(NetworkManager::init);
        fmlContext.getModEventBus().addListener(MIMIMod::init);

        // Deferred Registrations
        ModLootModifiers.REGISTER.register(fmlContext.getModEventBus());

        // Other Pre-Init
        ModConfigs.preInit(modContext);
    }
    
    public static void init(final FMLCommonSetupEvent event) {
        proxy.init(event);
    }
}
