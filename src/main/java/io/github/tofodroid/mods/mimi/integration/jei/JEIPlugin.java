package io.github.tofodroid.mods.mimi.integration.jei;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrument;
import io.github.tofodroid.mods.mimi.client.gui.GuiTuningTableContainerScreen;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.recipe.ModRecipes;
import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
	private static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, "jeiplugin");
	private static final Minecraft MC = Minecraft.getInstance();
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new TuningTableRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		RecipeManager manager = Objects.requireNonNull(MC.level).getRecipeManager();
		registration.addRecipes(TuningTableRecipeCategory.RECIPE_TYPE, manager.getRecipes().parallelStream().filter(recipe -> recipe.getType().equals(ModRecipes.TUNING_TYPE)).map(r -> (TuningTableRecipe) r).toList());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(ModItems.TUNINGTABLE), TuningTableRecipeCategory.RECIPE_TYPE);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(GuiTuningTableContainerScreen.class, 102, 39, 22, 15, TuningTableRecipeCategory.RECIPE_TYPE);
        registration.addGuiScreenHandler(GuiInstrument.class, new InstrumentGuiJEIHandler());
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}
}