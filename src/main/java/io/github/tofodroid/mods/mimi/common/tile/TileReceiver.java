package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerReceiver;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TileReceiver extends ATileInventory {
    public TileReceiver() {
        super(ModTiles.RECEIVER, 1);
    }

    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerReceiver(id, playerInventory, this.getPos());
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

    public Boolean shouldHandleMessage(UUID sender, Byte channel, Byte note, Boolean publicTransmit) {
        ItemStack switchStack = getSwitchboardStack();
        if(!switchStack.isEmpty()) {
            return ItemMidiSwitchboard.isChannelEnabled(switchStack, channel) && shouldPlayNote(note) &&
                ( 
                    (publicTransmit && ItemMidiSwitchboard.PUBLIC_SOURCE_ID.equals(ItemMidiSwitchboard.getMidiSource(switchStack))) 
                    || sender.equals(ItemMidiSwitchboard.getMidiSource(switchStack))
                );
        }
        return false;
    }

    public Boolean hasSwitchboard() {
        return !getSwitchboardStack().isEmpty();
    }
    
    protected Boolean shouldPlayNote(Byte note) {
        ItemStack switchStack = getSwitchboardStack();

        if(!switchStack.isEmpty()) {
            ArrayList<Byte> allowedNotes = ItemMidiSwitchboard.getFilterNotes(switchStack);

            if(allowedNotes != null && !allowedNotes.isEmpty()) {
                return allowedNotes.contains(note);
            } else {
                return true;
            }
        }
        return false;
    }
}
