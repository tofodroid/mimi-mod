package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.block.BlockListener;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
		return new TranslatableComponent(this.getBlockState().getBlock().asItem().getDescriptionId());
    }
    
    public Boolean shouldAcceptNote(Byte note, Byte instrumentId) {
        ItemStack switchStack = getSwitchboardStack();

        if(!switchStack.isEmpty()) {
            return ItemMidiSwitchboard.isNoteFiltered(switchStack, note) && ItemMidiSwitchboard.isInstrumentFiltered(switchStack, instrumentId);
        }

        return false;
    }

    @Override
    public void execServerTick(Level world, BlockPos pos, BlockState state, ANoteResponsiveTile self) {
        if (state.getValue(BlockListener.POWER) != 0) {
            world.setBlock(pos, state.setValue(BlockListener.POWER, Integer.valueOf(0)), 3);
 
            for(Direction direction : Direction.values()) {
                world.updateNeighborsAt(pos.relative(direction), ModBlocks.LISTENER.get());
            }
         }
    }
    
    @Override
    protected Boolean shouldHaveEntity() {
        return !this.getSwitchboardStack().isEmpty();
    }
}
