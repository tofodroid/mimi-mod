package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {
    
    @SubscribeEvent
    public static void registerTypes(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
        event.getRegistry().register(TuningTableRecipe.SERIALIZER);
        event.getRegistry().register(DyedInstrumentRecipe.SERIALIZER);
    }
}
