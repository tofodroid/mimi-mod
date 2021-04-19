package io.github.tofodroid.mods.mimi.common.recipes;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// ONLY USE FOR DYNAMIC RECIPES. STATIC RECIPES SHOULD USE JSON DATA.

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModRecipes {
    public static final SpecialRecipeSerializer<InsertDisk> INSERT_DISK = register("insert_disk", new SpecialRecipeSerializer<>(InsertDisk::new));
    public static final IRecipeSerializer<EjectDisk> EJECT_DISK = register("eject_disk", new SpecialRecipeSerializer<>(EjectDisk::new));

    @SubscribeEvent
    public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
        event.getRegistry().register(INSERT_DISK);
        event.getRegistry().register(EJECT_DISK);
    }

    private static <T extends IRecipeSerializer<? extends IRecipe<?>>> T register(final String name, final T t) {
        t.setRegistryName(new ResourceLocation(MIMIMod.MODID, name));
        return t;
    }
}
