package io.github.tofodroid.mods.mimi.common;

import net.minecraft.core.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.loot.ModLootModifiers;
import io.github.tofodroid.mods.mimi.common.mob.villager.ModVillagers;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.recipe.ModRecipes;
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
    }

    @SubscribeEvent
    public static void registerContent(final RegisterEvent event) {
        // Items
        event.register(Registry.ITEM_REGISTRY, (reg) -> {ModItems.submitRegistrations(reg);});

        // Tiles
        event.register(Registry.BLOCK_ENTITY_TYPE_REGISTRY, (reg) -> {ModTiles.submitRegistrations(reg);});

        // Recipes
        event.register(Registry.RECIPE_TYPE_REGISTRY, (reg) -> {ModRecipes.submitTypeRegistrations(reg);});
        event.register(Registry.RECIPE_SERIALIZER_REGISTRY, (reg) -> {ModRecipes.submitSerializerRegistrations(reg);});

        // Containers
        event.register(Registry.MENU_REGISTRY, (reg) -> {ModContainers.submitRegistrations(reg);});
    }
}
