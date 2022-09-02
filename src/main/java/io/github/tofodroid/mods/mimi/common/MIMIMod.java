package io.github.tofodroid.mods.mimi.common;

import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.loot.ModLootModifiers;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.common.mob.villager.ModVillagers;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.server.ServerProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MIMIMod.MODID)
public class MIMIMod {
    public static final String MODID = "mimi";
    public static final Logger LOGGER = LogManager.getLogger();
    public static Proxy proxy = (Proxy)DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public MIMIMod() {
        MIMIMod.preInit(FMLJavaModLoadingContext.get(), ModLoadingContext.get());
    }

    public static void preInit(FMLJavaModLoadingContext fmlContext, ModLoadingContext modContext) {
        // Configs
        ModConfigs.preInit(modContext);
        InstrumentConfig.preInit();

        // Event Listener Registration
        fmlContext.getModEventBus().addListener(MIMIMod::init);
        fmlContext.getModEventBus().addListener(NetworkManager::init);
        MinecraftForge.EVENT_BUS.addListener(ModVillagers::registerTrades);

        // Deferred Registrations
        ModEntities.ENTITY_TYPES.register(fmlContext.getModEventBus());
        ModLootModifiers.REGISTER.register(fmlContext.getModEventBus());

        // Blocks
        ModBlocks.BLOCKS.register(fmlContext.getModEventBus());

        // Village Stuff
        ModVillagers.POI_TYPES.register(fmlContext.getModEventBus());
        ModVillagers.PROFESSIONS.register(fmlContext.getModEventBus());
        ModVillagers.injectStructures();
    }
   
    public static void init(final FMLCommonSetupEvent event) {
        proxy.init(event);
        MidiFileCacheManager.init();
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void registerContent(final RegistryEvent.Register<?> event) {
        // Items
        if(event.getRegistry().getRegistryKey().equals(Registry.ITEM_REGISTRY)) {
            ModItems.submitRegistrations((RegistryEvent.Register<Item>)event);
        }

        // Tiles
        if(event.getRegistry().getRegistryKey().equals(Registry.BLOCK_ENTITY_TYPE_REGISTRY)) {
            ModTiles.submitRegistrations((RegistryEvent.Register<BlockEntityType<?>>)event);
        }

        // Containers
        if(event.getRegistry().getRegistryKey().equals(Registry.MENU_REGISTRY)) {
            ModContainers.submitRegistrations((RegistryEvent.Register<MenuType<?>>)event);       
        }
    }
}
