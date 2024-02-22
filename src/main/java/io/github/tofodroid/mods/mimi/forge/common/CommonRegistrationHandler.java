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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.CreativeModeTabEvent;
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
        // Event Based in 1.19.x and below
    }
    
    @SubscribeEvent
    public static void register(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(MIMIMod.MODID, "group"), builder -> ModItems.buildCreativeTab(builder));
    }

    @SubscribeEvent
    public static void postRegistrySetup(final FMLCommonSetupEvent event) {
        MIMIMod.postRegister();
    }
}
