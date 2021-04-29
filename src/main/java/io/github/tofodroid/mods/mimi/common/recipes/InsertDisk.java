/*
package io.github.tofodroid.mods.mimi.common.recipes;

import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class InsertDisk extends SpecialRecipe {
    public InsertDisk(ResourceLocation name) {
        super(name);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        return getCraftingResult(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack driveStack = null;
        ItemStack diskStack = null;

        // Find relevant stacks
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            stack = stack == null ? ItemStack.EMPTY : stack;

            if(!stack.isEmpty() && !(stack.getItem().equals(ModItems.DISK) || stack.getItem().equals(ModItems.DRIVE))) {
                return null;
            } else if((stack.getItem().equals(ModItems.DISK) && diskStack != null) || (stack.getItem().equals(ModItems.DRIVE) && driveStack != null)) {
                return null;
            } else if(stack.getItem().equals(ModItems.DISK)) {
                diskStack = stack;
            } else if(stack.getItem().equals(ModItems.DRIVE)) {
                driveStack = stack;
            }
        }

        // Validate stacks
        if(diskStack == null || driveStack == null || ModItems.DISK.isEmptyDisk(diskStack) || !ModItems.DRIVE.isEmptyDrive(driveStack)) {
            return null;
        }

        // Craft
        ItemStack resultStack = ModItems.DRIVE.insertDisk(diskStack, driveStack);

        return resultStack;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.INSERT_DISK;
    }
}
*/