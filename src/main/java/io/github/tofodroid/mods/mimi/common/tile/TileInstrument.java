package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TileInstrument extends ATileInventory {
    public static final String INSTRUMENT_ID_TAG = "instrument";

    private Byte instrumentId;
    
    public TileInstrument() {
        super(ModTiles.INSTRUMENT, 1);
    }

    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerInstrument(id, playerInventory, this.getInstrumentId(), this.getPos());
    }

    @Override
    public ITextComponent getDisplayName() {
		return new TranslationTextComponent(this.getBlockState().getBlock().asItem().getTranslationKey());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if(this.instrumentId != null) {
            compound.putByte(INSTRUMENT_ID_TAG, this.instrumentId);
        }

        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        if(compound.contains(INSTRUMENT_ID_TAG)) {
            this.instrumentId = compound.getByte(INSTRUMENT_ID_TAG);
        }
    }

    public Byte getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Byte instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getInstrumentName() {
        return getBlockState().getBlock().asItem().getName().getString();
    }

    public ItemStack getSwitchboardStack() {
        if(this.inventory.isPresent() && ModItems.SWITCHBOARD.equals(this.inventory.orElse(null).getStackInSlot(0).getItem())) {
            return this.inventory.orElse(null).getStackInSlot(0);
        }

        return ItemStack.EMPTY;
    }

    public Boolean shouldHandleMessage(UUID sender, Byte channel, Boolean publicTransmit) {
        ItemStack switchStack = getSwitchboardStack();
        if(!switchStack.isEmpty()) {
            return ItemMidiSwitchboard.isChannelEnabled(switchStack, channel) && 
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
}
