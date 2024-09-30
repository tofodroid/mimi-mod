package io.github.tofodroid.mods.mimi.common.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.ArrayList;

public class ColoredItemRecipe extends CustomRecipe {
    public static final String REGISTRY_NAME = "coloreditem";

	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<ColoredItemRecipe>(ColoredItemRecipe::new);

    public ColoredItemRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        ItemStack instrument = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stackI = inv.getItem(i);
            if (stackI.is(ItemTags.DYEABLE) && instrument.isEmpty()) {
                instrument = stackI;
            } else if (!stackI.isEmpty() && !(stackI.getItem() instanceof DyeItem))  {
                return false;
            }
        }

        return !instrument.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider pRegistries) {
        ItemStack instrument = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stackI = inv.getItem(i);
            if (!stackI.isEmpty()) {
                if (stackI.is(ItemTags.DYEABLE) && instrument.isEmpty()) {
                    instrument = stackI;
                } else if (stackI.getItem() instanceof DyeItem)  {
                    dyes.add((DyeItem)stackI.getItem());
                }
            }
        }

        return !instrument.isEmpty() && !dyes.isEmpty() ? DyedItemColor.applyDyes(instrument, dyes) : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ColoredItemRecipe.SERIALIZER;
    }
}