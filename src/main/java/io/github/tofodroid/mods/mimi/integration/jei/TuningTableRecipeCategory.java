package io.github.tofodroid.mods.mimi.integration.jei;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TuningTableRecipeCategory implements IRecipeCategory<TuningTableRecipe> {
	private final String title;
	private final IDrawable icon;
	private final IDrawable background;
	public static final ResourceLocation CATEGORY_UID = new ResourceLocation(MIMIMod.MODID, "tuning");
    public static final RecipeType<TuningTableRecipe> RECIPE_TYPE = new RecipeType<TuningTableRecipe>(CATEGORY_UID, TuningTableRecipe.class);

	public TuningTableRecipeCategory(IGuiHelper helper) {
        title = Component.translatable("gui.jei." + MIMIMod.MODID + ".tuning").getString();
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.TUNINGTABLE));
		background = helper.createDrawable(new ResourceLocation(MIMIMod.MODID, "textures/jei/tuning.png"), 0, 0, 59, 40);
	}

	@Override
	public RecipeType<TuningTableRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.literal(this.title);
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
    public void setRecipe(IRecipeLayoutBuilder recipeLayout, TuningTableRecipe recipe, IFocusGroup focusGroup) {
        recipeLayout.addSlot(RecipeIngredientRole.OUTPUT, 41, 22).addItemStack(recipe.getResultItem());
        recipeLayout.addSlot(RecipeIngredientRole.INPUT, 2, 2).addIngredients(recipe.getIngredients().get(0));
        recipeLayout.addSlot(RecipeIngredientRole.INPUT, 35, 2).addIngredients(recipe.getIngredients().get(1));
    }
}