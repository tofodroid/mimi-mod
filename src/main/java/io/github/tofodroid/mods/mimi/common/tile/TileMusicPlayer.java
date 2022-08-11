package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.BlockMusicPlayer;
import io.github.tofodroid.mods.mimi.common.container.ContainerMusicPlayer;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.server.midi.MusicPlayerMidiHandler;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class TileMusicPlayer extends AContainerTile implements BlockEntityTicker<TileMusicPlayer> {
    ItemStack lastDisk = null;
    Boolean wasPlaying = false;
    Boolean endOfMusicFlag = false;

    public TileMusicPlayer(BlockPos pos, BlockState state) {
        super(ModTiles.MUSICPLAYER, pos, state, 1);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    public static void doTick(Level world, BlockPos pos, BlockState state, TileMusicPlayer self) {
        self.tick(world, pos, state, self);
    }

    public ItemStack getActiveFloppyDiskStack() {
        if(items.isEmpty() || items.get(0) == null) {
            return ItemStack.EMPTY;
        } else {
            return items.get(0);
        }
    }

    public Boolean hasActiveFloppyDisk() {
        return !getActiveFloppyDiskStack().isEmpty();
    }

    public UUID getMusicPlayerId() {
        String idString = "tile-music-player-" + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return stack.getItem().equals(ModItems.FLOPPYDISK) && ItemFloppyDisk.isWritten(stack);
    }
    
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new ContainerMusicPlayer(id, playerInventory, this.getBlockPos());
    }

    @Override
    public Component getDefaultName() {
		return Component.translatable(this.getBlockState().getBlock().asItem().getDescriptionId());
    }

    public void endOfMusic() {
        this.endOfMusicFlag = true;
    }

    @Override
    public void setItem(int i, ItemStack item) {
        super.setItem(i, item);

        if(this.level instanceof ServerLevel && !this.level.isClientSide) {
            BlockState state = this.getBlockState().setValue(BlockMusicPlayer.POWER, 15);
            this.level.setBlock(this.getBlockPos(), state, 3);
            setChanged(this.level, this.getBlockPos(), state);
        }
    }
    
    @Override
    public void tick(Level world, BlockPos pos, BlockState state, TileMusicPlayer self) {
        if(this.hasLevel() && !this.level.isClientSide && world instanceof ServerLevel) {
            Boolean isPlaying = !(this.isRemoved() || this.getActiveFloppyDiskStack().isEmpty() || this.endOfMusicFlag);
            
            if(this.wasPlaying != isPlaying) {
                state = state.setValue(BlockMusicPlayer.POWER, isPlaying ? 15 : 0);
                world.setBlock(pos, state, 3);
                setChanged(world, pos, state);
            }

            if(this.hasActiveFloppyDisk()) {
                if(this.lastDisk != null && !this.lastDisk.equals(this.getActiveFloppyDiskStack())) {
                    ServerMusicPlayerMidiManager.removeMusicPlayer(this);
                    this.endOfMusicFlag = false;
                }

                if(!this.endOfMusicFlag) {
                    MusicPlayerMidiHandler handler = ServerMusicPlayerMidiManager.getOrAddMusicPlayer(this, ItemFloppyDisk.getMidiUrl(this.lastDisk));
                    if(handler != null && isPlaying) {
                        handler.play();
                    }
                }
            } else {
                ServerMusicPlayerMidiManager.removeMusicPlayer(this);
                this.endOfMusicFlag = false;
            }

            this.lastDisk = this.getActiveFloppyDiskStack();
            this.wasPlaying = isPlaying;
        }
    }
}