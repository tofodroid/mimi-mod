package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.container.ContainerAdvListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TileAdvListener extends ATileInventory {
    public TileAdvListener() {
        super(ModTiles.ADVLISTENER, 1);
    }

    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAdvListener(id, playerInventory, this.getPos());
    }

    @Override
    public ITextComponent getDisplayName() {
		return new TranslationTextComponent(this.getBlockState().getBlock().asItem().getTranslationKey());
    }

    public ItemStack getSwitchboardStack() {
        if(this.inventory.isPresent() && ModItems.SWITCHBOARD.equals(this.inventory.orElse(null).getStackInSlot(0).getItem())) {
            return this.inventory.orElse(null).getStackInSlot(0);
        }

        return ItemStack.EMPTY;
    }
    
    public Boolean shouldAcceptNote(Byte note, Byte instrumentId) {
        ItemStack switchStack = getSwitchboardStack();

        if(!switchStack.isEmpty()) {
            return ItemMidiSwitchboard.isNoteFiltered(switchStack, note) && ItemMidiSwitchboard.isInstrumentFiltered(switchStack, instrumentId);
        }

        return false;
    }
}
