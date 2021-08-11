package io.github.tofodroid.mods.mimi.common.tile;

import java.util.Arrays;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.inventory.MechanicalMaestroInventoryStackHandler;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class TileMechanicalMaestro extends ANoteResponsiveTile {
    public static final UUID MECH_UUID = new UUID(0,3);

    public Boolean hasEntity;

    public TileMechanicalMaestro() {
        super(ModTiles.MECHANICALMAESTRO, 2);
    }

    @Override
    public LazyOptional<? extends ItemStackHandler> buildInventory() {
        return LazyOptional.of(() -> new MechanicalMaestroInventoryStackHandler(INVENTORY_SIZE));
    }

    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerMechanicalMaestro(id, playerInventory, this.getPos());
    }

    @Override
    public ITextComponent getDisplayName() {
		return new TranslationTextComponent(this.getBlockState().getBlock().asItem().getTranslationKey());
    }

    public ItemStack getSwitchboardStack() {
        if(this.inventory.isPresent() && ModItems.SWITCHBOARD.equals(this.inventory.orElse(null).getStackInSlot(0).getItem())) {
            return this.inventory.orElse(null).getStackInSlot(0);
        }

        return ItemStack.EMPTY;
    }

    public ItemStack getInstrumentStack() {
        if(this.inventory.isPresent() && this.inventory.orElse(null).getStackInSlot(1).getItem() instanceof ItemInstrument || this.inventory.orElse(null).getStackInSlot(1).getItem() instanceof ItemInstrumentBlock) {
            return this.inventory.orElse(null).getStackInSlot(1);
        }

        return ItemStack.EMPTY;
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
        String idString = "tile-mech-maestro-" + this.getPos().getX() + "-" + this.getPos().getY() + "-" + this.getPos().getZ();
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }

    public Boolean shouldHandleMessage(UUID sender, Byte channel, Byte note, Boolean publicTransmit) {
        ItemStack switchStack = getSwitchboardStack();
        if(!switchStack.isEmpty() && getInstrumentId() != null) {
            return ItemMidiSwitchboard.isChannelEnabled(switchStack, channel) &&
                ( 
                    (publicTransmit && ItemMidiSwitchboard.PUBLIC_SOURCE_ID.equals(ItemMidiSwitchboard.getMidiSource(switchStack))) 
                    || sender.equals(ItemMidiSwitchboard.getMidiSource(switchStack))
                );
        }
        return false;
    }
    
    @Override
    public void tick() {
        if(tickCount >= UPDATE_EVERY_TICKS) {
            tickCount = 0;
            if(this.hasWorld() && !this.world.isRemote && !this.isRemoved()) {
                if(this.shouldHaveEntity()) {
                    EntityNoteResponsiveTile.create(this.world, this.pos);
                } else {
                    if(EntityNoteResponsiveTile.remove(this.world, this.pos)) {
                        this.allNotesOff();
                    }
                }
            }
        } else {
            tickCount ++;
        }        
    }

    @Override
    protected Boolean shouldHaveEntity() {
        return !this.getInstrumentStack().isEmpty() && !this.getSwitchboardStack().isEmpty() && this.world.isBlockPowered(this.getPos());
    }
    
    public void allNotesOff() {
		if(this.getInstrumentId() != null && this.getWorld() instanceof ServerWorld) {
			MidiNotePacketHandler.handlePacketsServer(
                Arrays.asList(new MidiNotePacket(MidiNotePacket.NO_CHANNEL, MidiNotePacket.ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), this.getInstrumentId(), this.getMaestroUUID(), true, this.getPos())),
                (ServerWorld)this.getWorld(),
                null
            );
		}
    }
}

