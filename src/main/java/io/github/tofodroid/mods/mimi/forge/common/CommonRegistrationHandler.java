package io.github.tofodroid.mods.mimi.forge.common;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.mob.villager.ModVillagers;
import io.github.tofodroid.mods.mimi.common.recipe.ModRecipes;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public abstract class CommonRegistrationHandler {    
    @SubscribeEvent
    public static void register(final RegisterEvent event) {
        // Blocks
        event.register(Registry.BLOCK_REGISTRY, (reg) -> ModBlocks.BLOCKS.forEach(reg::register));

        // Items
        event.register(Registry.ITEM_REGISTRY, (reg) -> ModItems.ITEMS.forEach(reg::register));

        // Entity Types
        event.register(Registry.ENTITY_TYPE_REGISTRY, (reg) -> ModEntities.ENTITES.forEach(reg::register));

        // Tiles
        event.register(Registry.BLOCK_ENTITY_TYPE_REGISTRY, (reg) -> ModTiles.BLOCK_ENTITIES.forEach(reg::register));

        // Recipes
        event.register(Registry.RECIPE_TYPE_REGISTRY, (reg) -> ModRecipes.RECIPES.forEach(reg::register));
        event.register(Registry.RECIPE_SERIALIZER_REGISTRY, (reg) -> ModRecipes.SERIALIZERS.forEach(reg::register));

        // Villagers
        event.register(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, (reg) -> ModVillagers.POI_TYPES.forEach(reg::register));
        event.register(Registry.VILLAGER_PROFESSION_REGISTRY, (reg) -> ModVillagers.PROFESSIONS.forEach(reg::register));

        // Container Menus
        event.register(Registry.MENU_REGISTRY, (reg) -> ModContainers.MENU_TYPES.forEach(reg::register));
    }

    @SubscribeEvent
    public static void postRegistrySetup(final FMLCommonSetupEvent event) {
        MIMIMod.postRegister();
    }
}
