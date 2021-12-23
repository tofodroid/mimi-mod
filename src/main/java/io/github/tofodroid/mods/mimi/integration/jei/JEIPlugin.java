package io.github.tofodroid.mods.mimi.integration.jei;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrumentContainerScreen;
import io.github.tofodroid.mods.mimi.client.gui.GuiTuningTableContainerScreen;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerTuningTable;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
	private static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, "jeiplugin");
	private static final Minecraft MC = Minecraft.getInstance();

	private static List<Recipe<?>> findRecipesByType(RecipeType<?> type) {
		return MC.level
				.getRecipeManager()
				.getRecipes()
				.stream()
				.filter(r -> r.getType() == type)
				.collect(Collectors.toList());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new TuningTableRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(findRecipesByType(TuningTableRecipe.TYPE), new ResourceLocation(MIMIMod.MODID, "tuning"));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(ModItems.TUNINGTABLE), new ResourceLocation(MIMIMod.MODID, "tuning"));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(GuiTuningTableContainerScreen.class, 102, 39, 22, 15, new ResourceLocation(MIMIMod.MODID, "tuning"));
        registration.addGuiScreenHandler(GuiInstrumentContainerScreen.class, NoJEIGuiProperties::new);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(ContainerTuningTable.class, new ResourceLocation(MIMIMod.MODID, "tuning"), ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID, 2, 0, ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}
}
