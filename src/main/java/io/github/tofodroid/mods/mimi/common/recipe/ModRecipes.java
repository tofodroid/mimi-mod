package io.github.tofodroid.mods.mimi.common.recipe;

import java.util.Map;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
    public static RecipeType<TuningTableRecipe> TUNING_TYPE = new RecipeType<TuningTableRecipe>() {
        public String toString() {
           return new ResourceLocation(MIMIMod.MODID, TuningTableRecipe.Serializer.REGISTRY_NAME).toString();
        }
    };

    public static final Map<ResourceLocation, RecipeType<?>> RECIPES = Map.of(
        new ResourceLocation(MIMIMod.MODID, TuningTableRecipe.Serializer.REGISTRY_NAME), TUNING_TYPE
    );

    public static final Map<ResourceLocation, RecipeSerializer<?>> SERIALIZERS = Map.of(
        new ResourceLocation(MIMIMod.MODID, TuningTableRecipe.Serializer.REGISTRY_NAME), TuningTableRecipe.SERIALIZER,
        new ResourceLocation(MIMIMod.MODID, CloneMidiSettingsRecipe.REGISTRY_NAME), CloneMidiSettingsRecipe.SERIALIZER,
        new ResourceLocation(MIMIMod.MODID, CopyBlockDyeRecipe.REGISTRY_NAME), CopyBlockDyeRecipe.SERIALIZER,
        new ResourceLocation(MIMIMod.MODID, ChangeLedCubeRecipe.REGISTRY_NAME), ChangeLedCubeRecipe.SERIALIZER,
        new ResourceLocation(MIMIMod.MODID, ColoredItemRecipe.REGISTRY_NAME), ColoredItemRecipe.SERIALIZER
    );
}
