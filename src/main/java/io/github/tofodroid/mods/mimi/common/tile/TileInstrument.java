package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TileInstrument extends ATileInventory {
    public static final String COLOR_TAG = "color";

    protected Integer color;

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

        if(this.color != null) {
            compound.putInt(COLOR_TAG, color);
        }

        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        if(compound.contains(COLOR_TAG)) {
            this.color = compound.getInt(COLOR_TAG);
        }
    }

    public Byte getInstrumentId() {
        return ((BlockInstrument)getBlockState().getBlock()).getInstrumentId();
    }

    public void setColor(Integer color) {
        if(((BlockInstrument)getBlockState().getBlock()).isDyeable()) {
            this.color = color;
        }
    }

    public Boolean hasColor() {
        return color != null && ((BlockInstrument)getBlockState().getBlock()).isDyeable();
    }

    public Integer getColor() { 
        if(!((BlockInstrument)getBlockState().getBlock()).isDyeable()) {
            return -1;
        }

        return hasColor() ? color : ((BlockInstrument)getBlockState().getBlock()).getDefaultColor();
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
                    || (sender != null && sender.equals(ItemMidiSwitchboard.getMidiSource(switchStack)))
                );
        }
        return false;
    }

    public Boolean hasSwitchboard() {
        return !getSwitchboardStack().isEmpty();
    }
}
