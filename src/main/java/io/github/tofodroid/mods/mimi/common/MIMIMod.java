package io.github.tofodroid.mods.mimi.common;

import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.gui.GuiWrapper;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.loot.ModLootModifiers;
import io.github.tofodroid.mods.mimi.common.mob.villager.ModVillagers;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.recipe.ModRecipes;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.server.ServerGuiWrapper;
import io.github.tofodroid.mods.mimi.server.ServerProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        ModEntities.ENTITY_TYPES.register(fmlContext.getModEventBus());
        ModLootModifiers.REGISTER.register(fmlContext.getModEventBus());

        // Other Pre-Init
        ModConfigs.preInit(modContext);
        InstrumentConfig.preInit();
    }
    
    public static void init(final FMLCommonSetupEvent event) {
        proxy.init(event);

        // Other Init
        event.enqueueWork(() -> ModVillagers.injectStructures());
    }

    @SubscribeEvent
    public static void registerContent(final RegisterEvent event) {
        // Blocks
        event.register(Registry.BLOCK_REGISTRY, (reg) -> {ModBlocks.submitRegistrations(reg);});

        // Items
        event.register(Registry.ITEM_REGISTRY, (reg) -> {ModItems.submitRegistrations(reg);});

        // Tiles
        event.register(Registry.BLOCK_ENTITY_TYPE_REGISTRY, (reg) -> {ModTiles.submitRegistrations(reg);});

        // Entities
        //event.register(Registry.ENTITY_TYPE_REGISTRY, (reg) -> {ModEntities.submitRegistrations(reg);});

        // Recipes
        event.register(Registry.RECIPE_TYPE_REGISTRY, (reg) -> {ModRecipes.submitTypeRegistrations(reg);});
        event.register(Registry.RECIPE_SERIALIZER_REGISTRY, (reg) -> {ModRecipes.submitSerializerRegistrations(reg);});

        // Containers
        event.register(Registry.MENU_REGISTRY, (reg) -> {ModContainers.submitRegistrations(reg);});

        // Village Stuff
        event.register(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, (reg) -> {ModVillagers.registerPoiTypes(reg);});
        event.register(Registry.VILLAGER_PROFESSION_REGISTRY, (reg) -> {ModVillagers.registerProfessions(reg);});
    }
}
