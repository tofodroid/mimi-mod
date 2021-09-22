package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerConductor;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacketHandler;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TileConductor extends ATileInventory {
    public Byte lastNote = null;
    public Boolean lastBroadcastPublic = null;
    public ArrayList<Byte> lastChannels = new ArrayList<>();

    public TileConductor() {
        super(ModTiles.CONDUCTOR, 1);
    }

    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerConductor(id, playerInventory, this.getPos());
    }

    @Override
    public ITextComponent getDisplayName() {
		return new TranslationTextComponent(this.getBlockState().getBlock().asItem().getTranslationKey());
    }

    @Override
    public void remove() {
        super.remove();
        transmitNoteOff(this.getWorld());
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

    public void transmitNoteOn(World worldIn) {
        if(!world.isRemote && world instanceof ServerWorld && lastNote == null) {
            ItemStack switchStack = getSwitchboardStack();

            if(!switchStack.isEmpty()) {
                lastNote = ItemMidiSwitchboard.getBroadcastNote(switchStack);
                lastBroadcastPublic = ItemMidiSwitchboard.getPublicBroadcast(switchStack);

                for(Byte channel : ItemMidiSwitchboard.getEnabledChannelsSet(switchStack)) {
                    lastChannels.add(channel);
                    TransmitterNotePacket packet =  new TransmitterNotePacket(channel, lastNote, Byte.MAX_VALUE, lastBroadcastPublic ? TransmitMode.PUBLIC : TransmitMode.LINKED);
                    TransmitterNotePacketHandler.handlePacketServer(packet, this.getPos(), (ServerWorld)worldIn, getUniqueId(), null);
                }
            }
        }
    }

    public void transmitNoteOff(World worldIn) {
        if(!world.isRemote && world instanceof ServerWorld) {
            if(!lastChannels.isEmpty() && lastNote != null) {
                for(Byte channel : lastChannels) {
                    TransmitterNotePacket packet =  new TransmitterNotePacket(channel, lastNote, new Integer(-1).byteValue(), lastBroadcastPublic ? TransmitMode.PUBLIC : TransmitMode.LINKED);
                    TransmitterNotePacketHandler.handlePacketServer(packet, this.getPos(), (ServerWorld)worldIn, getUniqueId(), null);
                }
            }
            lastChannels = new ArrayList<>();
            lastNote = null;
            lastBroadcastPublic = null;
        }
    }

    public UUID getUniqueId() {
        String posString = "con" + this.getPos().getX() + this.getPos().getY() + this.getPos().getZ();
        return UUID.nameUUIDFromBytes(posString.getBytes());
    }
}
