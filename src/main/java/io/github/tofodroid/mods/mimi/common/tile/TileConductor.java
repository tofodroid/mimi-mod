package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerConductor;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacketHandler;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileConductor extends ATileInventory {
    public Byte lastNote = null;
    public Boolean lastBroadcastPublic = null;
    public ArrayList<Byte> lastChannels = new ArrayList<>();

    public TileConductor(BlockPos pos, BlockState state) {
        super(ModTiles.CONDUCTOR, pos, state, 1);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerConductor(id, playerInventory, this.getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        transmitNoteOff(this.getLevel());
    }

    public ItemStack getSwitchboardStack() {
        if(this.inventory.isPresent() && ModItems.SWITCHBOARD.equals(this.inventory.orElse(null).getStackInSlot(0).getItem())) {
            return this.inventory.orElse(null).getStackInSlot(0);
        }

        return ItemStack.EMPTY;
    }

    public Boolean hasSwitchboard() {
        return !getSwitchboardStack().isEmpty();
    }

    public void transmitNoteOn(Level worldIn) {
        if(!worldIn.isClientSide && worldIn instanceof ServerLevel && lastNote == null) {
            ItemStack switchStack = getSwitchboardStack();

            if(!switchStack.isEmpty()) {
                lastNote = ItemMidiSwitchboard.getBroadcastNote(switchStack);
                lastBroadcastPublic = ItemMidiSwitchboard.getPublicBroadcast(switchStack);

                for(Byte channel : ItemMidiSwitchboard.getEnabledChannelsSet(switchStack)) {
                    lastChannels.add(channel);
                    TransmitterNotePacket packet =  new TransmitterNotePacket(channel, lastNote, Byte.MAX_VALUE, lastBroadcastPublic ? TransmitMode.PUBLIC : TransmitMode.LINKED);
                    TransmitterNotePacketHandler.handlePacketServer(packet, this.getBlockPos(), (ServerLevel)worldIn, getUniqueId(), null);
                }
            }
        }
    }

    public void transmitNoteOff(Level worldIn) {
        if(!worldIn.isClientSide && worldIn instanceof ServerLevel) {
            if(!lastChannels.isEmpty() && lastNote != null) {
                for(Byte channel : lastChannels) {
                    TransmitterNotePacket packet =  new TransmitterNotePacket(channel, lastNote, Integer.valueOf(-1).byteValue(), lastBroadcastPublic ? TransmitMode.PUBLIC : TransmitMode.LINKED);
                    TransmitterNotePacketHandler.handlePacketServer(packet, this.getBlockPos(), (ServerLevel)worldIn, getUniqueId(), null);
                }
            }
            lastChannels = new ArrayList<>();
            lastNote = null;
            lastBroadcastPublic = null;
        }
    }

    public UUID getUniqueId() {
        String posString = "con" + this.getBlockPos().getX() + this.getBlockPos().getY() + this.getBlockPos().getZ();
        return UUID.nameUUIDFromBytes(posString.getBytes());
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent(this.getBlockState().getBlock().asItem().getDescriptionId());
    }
}
