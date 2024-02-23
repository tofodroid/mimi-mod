package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.item.IColorableItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack instrument = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if (IColorableItem.isDyeableInstrument(stackI) && instrument.isEmpty()) {
                instrument = stackI;
            } else if (!stackI.isEmpty() && !(stackI.getItem() instanceof DyeItem))  {
                return false;
            }
        }

        return !instrument.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess r) {
        ItemStack instrument = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stackI = inv.getItem(i);
            if (!stackI.isEmpty()) {
                if (IColorableItem.isDyeableInstrument(stackI) && instrument.isEmpty()) {
                    instrument = stackI;
                } else if (stackI.getItem() instanceof DyeItem)  {
                    dyes.add((DyeItem)stackI.getItem());
                }
            }
        }

        return !instrument.isEmpty() && !dyes.isEmpty() ? IColorableItem.dyeItem(instrument, dyes) : ItemStack.EMPTY;
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