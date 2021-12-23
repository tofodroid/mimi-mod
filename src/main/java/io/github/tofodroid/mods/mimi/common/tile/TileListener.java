package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileListener extends ANoteResponsiveTile {
    public TileListener(BlockPos pos, BlockState state) {
        super(ModTiles.LISTENER, pos, state, 1);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerListener(id, playerInventory, this.getBlockPos());
    }

    @Override
    public Component getDisplayName() {
		return new TranslatableComponent(this.getBlockState().getBlock().asItem().getDescriptionId());
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
    
    @Override
    protected Boolean shouldHaveEntity() {
        return !this.getSwitchboardStack().isEmpty();
    }
}
