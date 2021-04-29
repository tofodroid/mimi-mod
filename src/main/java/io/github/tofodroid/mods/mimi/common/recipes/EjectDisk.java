/*
package io.github.tofodroid.mods.mimi.common.recipes;

import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EjectDisk extends SpecialRecipe {
    public EjectDisk(ResourceLocation name) {
        super(name);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        return getCraftingResult(inv) != null;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> result = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        result.set(0, new ItemStack(ModItems.DRIVE));
        return result;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack driveStack = null;

        // Find relevant stack
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            stack = stack == null ? ItemStack.EMPTY : stack;

            if(!stack.isEmpty() && !stack.getItem().equals(ModItems.DRIVE)) {
                return null;
            } else if(stack.getItem().equals(ModItems.DRIVE) && driveStack != null) {
                return null;
            } else if(stack.getItem().equals(ModItems.DRIVE)) {
                driveStack = stack;
            }
        }

        // Validate stack
        if(ModItems.DRIVE.isEmptyDrive(driveStack)) {
            return null;
        }

        // Craft
        ItemStack resultStack = ModItems.DRIVE.ejectDisk(driveStack);

        return resultStack;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.EJECT_DISK;
    }
    
}
*/