package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegisterEvent;

public class ModRecipes {
    public static RecipeType<TuningTableRecipe> TUNING_TYPE = new RecipeType<TuningTableRecipe>() {
        public String toString() {
           return new ResourceLocation(MIMIMod.MODID, "tuning").toString();
        }
     };

    public static void submitTypeRegistrations(final RegisterEvent.RegisterHelper<RecipeType<?>> event) {
        event.register(TuningTableRecipe.Serializer.REGISTRY_NAME, TUNING_TYPE);
    }

    public static void submitSerializerRegistrations(final RegisterEvent.RegisterHelper<RecipeSerializer<?>> event) {
        event.register(TuningTableRecipe.Serializer.REGISTRY_NAME, TuningTableRecipe.SERIALIZER);
        event.register(DyedItemRecipe.REGISTRY_NAME, DyedItemRecipe.SERIALIZER);
    }

}
