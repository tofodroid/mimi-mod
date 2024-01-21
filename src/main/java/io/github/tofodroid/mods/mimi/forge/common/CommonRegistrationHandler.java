package io.github.tofodroid.mods.mimi.forge.common;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.mob.villager.ModVillagers;
import io.github.tofodroid.mods.mimi.common.recipe.ModRecipes;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public abstract class CommonRegistrationHandler {    
    @SubscribeEvent
    public static void register(final RegisterEvent event) {
        // Blocks
        event.register(Registries.BLOCK, (reg) -> ModBlocks.BLOCKS.forEach(reg::register));

        // Items
        event.register(Registries.ITEM, (reg) -> ModItems.ITEMS.forEach(reg::register));

        // Entity Types
        event.register(Registries.ENTITY_TYPE, (reg) -> ModEntities.ENTITES.forEach(reg::register));

        // Tiles
        event.register(Registries.BLOCK_ENTITY_TYPE, (reg) -> ModTiles.BLOCK_ENTITIES.forEach(reg::register));

        // Recipes
        event.register(Registries.RECIPE_TYPE, (reg) -> ModRecipes.RECIPES.forEach(reg::register));
        event.register(Registries.RECIPE_SERIALIZER, (reg) -> ModRecipes.SERIALIZERS.forEach(reg::register));

        // Villagers
        event.register(Registries.POINT_OF_INTEREST_TYPE, (reg) -> ModVillagers.POI_TYPES.forEach(reg::register));
        event.register(Registries.VILLAGER_PROFESSION, (reg) -> ModVillagers.PROFESSIONS.forEach(reg::register));

        // Container Menus
        event.register(Registries.MENU, (reg) -> ModContainers.MENU_TYPES.forEach(reg::register));

        // Create Tabs
        event.register(Registries.CREATIVE_MODE_TAB, (reg) -> ModItems.CREATIVE_TABS.forEach(reg::register));
    }

    @SubscribeEvent
    public static void postRegistrySetup(final FMLCommonSetupEvent event) {
        MIMIMod.postRegister();
    }
}
