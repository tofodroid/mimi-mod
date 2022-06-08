package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileListener extends ANoteResponsiveTile {
    public TileListener(BlockPos pos, BlockState state) {
        super(ModTiles.LISTENER, pos, state, 1);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new ContainerListener(id, playerInventory, this.getBlockPos());
    }

    @Override
    public Component getDefaultName() {
		return Component.translatable(this.getBlockState().getBlock().asItem().getDescriptionId());
    }
    
    public Boolean shouldAcceptNote(Byte note, Byte instrumentId) {
        ItemStack switchStack = getSwitchboardStack();

        if(!switchStack.isEmpty()) {
            return ItemMidiSwitchboard.isNoteFiltered(switchStack, note) && ItemMidiSwitchboard.isInstrumentFiltered(switchStack, instrumentId);
        }

        return false;
    }
    
    @Override
    protected Boolean shouldHaveEntity() {
        return !this.getSwitchboardStack().isEmpty();
    }
}
