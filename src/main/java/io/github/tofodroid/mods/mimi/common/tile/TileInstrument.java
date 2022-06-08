package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;

public class TileInstrument extends ASwitchboardContainerEntity {
    public static final String COLOR_TAG = "color";

    protected Integer color;

    public TileInstrument(BlockPos pos, BlockState state) {
        super(ModTiles.INSTRUMENT, pos, state, 1);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if(this.color != null) {
            compound.putInt(COLOR_TAG, color);
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

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
        return getBlockState().getBlock().asItem().getDescription().getString();
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

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new ContainerInstrument(id, playerInventory, this.getInstrumentId(), this.getBlockPos());
    }

    @Override
    public Component getDefaultName() {
		return Component.translatable(this.getBlockState().getBlock().asItem().getDescriptionId());
    }
}
