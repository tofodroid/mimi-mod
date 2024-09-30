package io.github.tofodroid.mods.mimi.common.recipe;

import java.util.Map;

import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
    public static RecipeType<TuningTableRecipe> TUNING_TYPE = new RecipeType<TuningTableRecipe>() {
        public String toString() {
           return ResourceUtils.newModLocation(TuningTableRecipe.Serializer.REGISTRY_NAME).toString();
        }
    };

    public static RecipeType<ChangeLedCubeRecipe> CHANGE_LED_TYPE = new RecipeType<ChangeLedCubeRecipe>() {
        public String toString() {
           return ResourceUtils.newModLocation(ChangeLedCubeRecipe.ChangeLedSerializer.REGISTRY_NAME).toString();
        }
    };

    public static final Map<ResourceLocation, RecipeType<?>> RECIPES = Map.of(
        ResourceUtils.newModLocation(TuningTableRecipe.Serializer.REGISTRY_NAME), TUNING_TYPE,
        ResourceUtils.newModLocation(ChangeLedCubeRecipe.ChangeLedSerializer.REGISTRY_NAME), CHANGE_LED_TYPE
    );

    public static final Map<ResourceLocation, RecipeSerializer<?>> SERIALIZERS = Map.of(
        ResourceUtils.newModLocation(TuningTableRecipe.Serializer.REGISTRY_NAME), TuningTableRecipe.SERIALIZER,
        ResourceUtils.newModLocation(ChangeLedCubeRecipe.ChangeLedSerializer.REGISTRY_NAME), ChangeLedCubeRecipe.SERIALIZER,
        ResourceUtils.newModLocation(CloneMidiSettingsRecipe.REGISTRY_NAME), CloneMidiSettingsRecipe.SERIALIZER,
        ResourceUtils.newModLocation(CloneEffectEmitterRecipe.REGISTRY_NAME), CloneEffectEmitterRecipe.SERIALIZER,
        ResourceUtils.newModLocation(CopyBlockDyeRecipe.REGISTRY_NAME), CopyBlockDyeRecipe.SERIALIZER,
        ResourceUtils.newModLocation(ColoredItemRecipe.REGISTRY_NAME), ColoredItemRecipe.SERIALIZER
    );
}
