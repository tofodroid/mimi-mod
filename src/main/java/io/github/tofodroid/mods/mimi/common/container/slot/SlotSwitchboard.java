package io.github.tofodroid.mods.mimi.common.container.slot;

import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.github.tofodroid.mods.mimi.common.item.ModItems;

public class SlotSwitchboard extends Slot {
    private static Container emptyInventory = new SimpleContainer(1);
    private final IItemHandler itemHandler;
    private final int index;

    public SlotSwitchboard(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(emptyInventory, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        return ModItems.SWITCHBOARD.equals(stack.getItem());
    }

    @Override
    @NotNull
    public ItemStack getItem()
    {
        return this.getItemHandler().getStackInSlot(index);
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    @Override
    public void set(@NotNull ItemStack stack)
    {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, stack);
        this.setChanged();
    }

    @Override
    public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn)
    {

    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack)
    {
        return 1;
    }

    @Override
    public boolean mayPickup(Player playerIn)
    {
        return !this.getItemHandler().extractItem(index, 1, true).isEmpty();
    }

    @Override
    @NotNull
    public ItemStack remove(int amount)
    {
        return this.getItemHandler().extractItem(index, amount, false);
    }

    public IItemHandler getItemHandler()
    {
        return itemHandler;
    }
}
