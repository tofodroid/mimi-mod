package io.github.tofodroid.mods.mimi.common.recipe;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {
    public static final Supplier<RecipeType<TuningTableRecipe>> TUNING = register("mimi:tuning");

    public static void registerTypes() {
        TUNING.get();
    }

    @SubscribeEvent
    public static void registerSerializers(final RegistryEvent.Register<RecipeSerializer<?>> event) {
        registerTypes();
        event.getRegistry().register(TuningTableRecipe.SERIALIZER);
        event.getRegistry().register(DyedInstrumentRecipe.SERIALIZER);
    }

    private static <R extends Recipe<?>> Supplier<RecipeType<R>> register(String name){
        return Lazy.of(() -> RecipeType.register(name));
    }
}
