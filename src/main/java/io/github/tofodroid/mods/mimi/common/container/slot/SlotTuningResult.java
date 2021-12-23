package io.github.tofodroid.mods.mimi.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotTuningResult extends Slot {
    private final Container container;
    public final Inventory craftingContainer;

    public SlotTuningResult(Container container, Inventory craftingContainer, Inventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.container = container;
        this.craftingContainer = craftingContainer;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    /*
    @Override
    public void onTake(Player player, ItemStack stack) {
        NonNullList<ItemStack> remaining;

        remaining = player.level.getRecipeManager().getRemainingItemsFor(TuningTableRecipe.TYPE, this.craftingContainer, player.level);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack slotStack = this.craftingContainer.getItem(i);
            ItemStack remainingStack = remaining.get(i);

            if (!slotStack.isEmpty()) {
                this.craftingContainer.removeItem(i, 1);
            }

            if (!remainingStack.isEmpty()) {
                this.craftingContainer.setItem(i, remainingStack);
            }
        }

        this.container.setChanged();
    }
    */
}