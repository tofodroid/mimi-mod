package io.github.tofodroid.mods.mimi.common.container.slot;

import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class SlotTuningResult extends Slot {
    private final Container container;
    private final IInventory matrix;

    public SlotTuningResult(Container container, IInventory matrix, IInventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.container = container;
        this.matrix = matrix;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack onTake(PlayerEntity player, ItemStack stack) {
        NonNullList<ItemStack> remaining;

        remaining = player.world.getRecipeManager().getRecipeNonNull(TuningTableRecipe.TYPE, this.matrix, player.world);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack slotStack = this.matrix.getStackInSlot(i);
            ItemStack remainingStack = remaining.get(i);

            if (!slotStack.isEmpty()) {
                this.matrix.removeStackFromSlot(i);
            }

            if (!remainingStack.isEmpty()) {
                this.matrix.setInventorySlotContents(i, remainingStack);
            }
        }

        this.container.onCraftMatrixChanged(this.matrix);

        return stack;
    }
}