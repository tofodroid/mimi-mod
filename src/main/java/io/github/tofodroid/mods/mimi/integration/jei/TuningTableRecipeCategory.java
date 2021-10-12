package io.github.tofodroid.mods.mimi.integration.jei;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerTuningTable;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
public class TuningTableRecipeCategory implements IRecipeCategory<TuningTableRecipe> {
	private final String title;
	private final IDrawable icon;
	private final IDrawable background;

	public TuningTableRecipeCategory(IGuiHelper helper) {
        title = new TranslationTextComponent("gui.jei." + MIMIMod.MODID + ".tuning").getString();
		icon = helper.createDrawableIngredient(new ItemStack(ModItems.TUNINGTABLE));
		background = helper.createDrawable(new ResourceLocation(MIMIMod.MODID, "textures/jei/tuning.png"), 0, 0, 59, 40);
	}

	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(MIMIMod.MODID, "tuning");
	}

	@Override
	public Class<? extends TuningTableRecipe> getRecipeClass() {
		return TuningTableRecipe.class;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setIngredients(TuningTableRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(recipe.getRecipeOutput()));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, TuningTableRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		// Draw inputs
		itemStacks.init(ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID, true, 1, 1);
		itemStacks.set(ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));
		itemStacks.init(ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID+1, true, 34, 1);
		itemStacks.set(ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID+1, Arrays.asList(recipe.getIngredients().get(1).getMatchingStacks()));

        // Draw output
		itemStacks.init(ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID+2, false, 40, 21);
        itemStacks.set(ContainerTuningTable.TARGET_CONTAINER_MIN_SLOT_ID+2, Arrays.asList(recipe.getRecipeOutput()));
	}
}