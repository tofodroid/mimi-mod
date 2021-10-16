package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.IDyeableInstrumentItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;
import java.util.ArrayList;

public class DyedInstrumentRecipe extends SpecialRecipe {
	public static final IRecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<>(DyedInstrumentRecipe::new).setRegistryName(MIMIMod.MODID, "dyedinstrument");

    public DyedInstrumentRecipe(ResourceLocation recipeId) {
        super(recipeId);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack instrument = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stackI = inv.getStackInSlot(i);
            if (IDyeableInstrumentItem.isDyeableInstrument(stackI) && instrument.isEmpty()) {
                instrument = stackI;
            } else if (!stackI.isEmpty() && !(stackI.getItem() instanceof DyeItem))  {
                return false;
            }
        }

        return !instrument.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack instrument = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stackI = inv.getStackInSlot(i);
            if (!stackI.isEmpty()) {
                if (IDyeableInstrumentItem.isDyeableInstrument(stackI) && instrument.isEmpty()) {
                    instrument = stackI;
                } else if (stackI.getItem() instanceof DyeItem)  {
                    dyes.add((DyeItem)stackI.getItem());
                }
            }
        }

        return !instrument.isEmpty() && !dyes.isEmpty() ? IDyeableInstrumentItem.dyeItem(instrument, dyes) : ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return DyedInstrumentRecipe.SERIALIZER;
    }
}