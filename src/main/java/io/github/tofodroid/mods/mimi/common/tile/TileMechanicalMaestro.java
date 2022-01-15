package io.github.tofodroid.mods.mimi.common.tile;

import java.util.Arrays;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileMechanicalMaestro extends ANoteResponsiveTile {
    public static final UUID MECH_UUID = new UUID(0,3);

    public Boolean hasEntity;

    public TileMechanicalMaestro(BlockPos pos, BlockState state) {
        super(ModTiles.MECHANICALMAESTRO, pos, state, 2);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new ContainerMechanicalMaestro(id, playerInventory, this.getBlockPos());
    }

    @Override
    public Component getDefaultName() {
		return new TranslatableComponent(this.getBlockState().getBlock().asItem().getDescriptionId());
    }

    public ItemStack getInstrumentStack() {
        return items.isEmpty() ? ItemStack.EMPTY : items.get(1);
    }

    public Byte getInstrumentId() {
        ItemStack instrumentStack = this.getInstrumentStack();

        if(!instrumentStack.isEmpty()) {
            if(instrumentStack.getItem() instanceof ItemInstrument) {
                return ((ItemInstrument)instrumentStack.getItem()).getInstrumentId();
            } else if(instrumentStack.getItem() instanceof ItemInstrumentBlock) {
                return ((ItemInstrumentBlock)instrumentStack.getItem()).getInstrumentId();
            }
        }

        return null;
    }

    public UUID getMaestroUUID() {
        String idString = "tile-mech-maestro-" + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }

    public Boolean shouldHandleMessage(UUID sender, Byte channel, Byte note, Boolean publicTransmit) {
        ItemStack switchStack = getSwitchboardStack();
        if(!switchStack.isEmpty() && getInstrumentId() != null) {
            return ItemMidiSwitchboard.isChannelEnabled(switchStack, channel) &&
                ( 
                    (publicTransmit && ItemMidiSwitchboard.PUBLIC_SOURCE_ID.equals(ItemMidiSwitchboard.getMidiSource(switchStack))) 
                    || (sender != null && sender.equals(ItemMidiSwitchboard.getMidiSource(switchStack)))
                );
        }
        return false;
    }
    
    @Override
    public void tick(Level world, BlockPos pos, BlockState state, ANoteResponsiveTile self) {
        if(tickCount >= UPDATE_EVERY_TICKS) {
            tickCount = 0;
            if(this.hasLevel() && !this.level.isClientSide) {
                if(this.shouldHaveEntity() && !this.isRemoved()) {
                    EntityNoteResponsiveTile.create(this.level, this.getBlockPos());
                } else if(EntityNoteResponsiveTile.entityExists(this.level, Double.valueOf(this.getBlockPos().getX()), Double.valueOf(this.getBlockPos().getY()), Double.valueOf(this.getBlockPos().getZ()))) {
                    EntityNoteResponsiveTile.remove(this.level, this.getBlockPos());
                    this.allNotesOff();
                }
            }
        } else {
            tickCount ++;
        }        
    }

    @Override
    public void setItem(int i, ItemStack item) {
        this.allNotesOff();
        super.setItem(i, item);
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        this.allNotesOff();
        ItemStack result = super.removeItem(i, count);
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        this.allNotesOff();
        ItemStack result = super.removeItemNoUpdate(i);
        return result;
    }
    
    @Override
    public void clearContent() {
        this.allNotesOff();
        super.clearContent();
    }

    @Override
    protected Boolean shouldHaveEntity() {
        return !this.getInstrumentStack().isEmpty() && !this.getSwitchboardStack().isEmpty() && this.level.hasNeighborSignal(this.getBlockPos());
    }
    
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return (slot == 0 && stack.getItem().equals(ModItems.SWITCHBOARD)) || (slot == 1 && (stack.getItem() instanceof ItemInstrument || stack.getItem() instanceof ItemInstrumentBlock));
    }
    
    public void allNotesOff() {
		if(this.getInstrumentId() != null && this.getLevel() instanceof ServerLevel) {
			MidiNotePacketHandler.handlePacketsServer(
                Arrays.asList(new MidiNotePacket(MidiNotePacket.ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), this.getInstrumentId(), TileMechanicalMaestro.MECH_UUID, this.getBlockPos())),
                (ServerLevel)this.getLevel(),
                null
            );
		}
    }
}

