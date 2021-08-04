package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TileMechanicalMaestro extends ATileInventory {
    public static final UUID MECH_UUID = new UUID(0,3);

    public TileMechanicalMaestro() {
        super(ModTiles.MECHANICALMAESTRO, 2);
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
        // Get mech maestro switch stack if present, else get instrument switch stack if present
        if(this.inventory.isPresent() && ModItems.SWITCHBOARD.equals(this.inventory.orElse(null).getStackInSlot(0).getItem())) {
            return this.inventory.orElse(null).getStackInSlot(0);
        } else if(this.getInstrumentStack().getItem() instanceof ItemInstrument && !ItemInstrument.getSwitchboardStack(this.getInstrumentStack()).isEmpty()) {
            return ItemInstrument.getSwitchboardStack(this.getInstrumentStack());
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
        String idString = "tile-mech-maestro-" + this.getPos().getCoordinatesAsString().replace(", ", "-");
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }

    public Boolean shouldHandleMessage(UUID sender, Byte channel, Byte note, Boolean publicTransmit) {
        ItemStack switchStack = getSwitchboardStack();
        if(getInstrumentId() != null && !switchStack.isEmpty()) {
            return ItemMidiSwitchboard.isChannelEnabled(switchStack, channel) &&
                ( 
                    (publicTransmit && ItemMidiSwitchboard.PUBLIC_SOURCE_ID.equals(ItemMidiSwitchboard.getMidiSource(switchStack))) 
                    || sender.equals(ItemMidiSwitchboard.getMidiSource(switchStack))
                );
        }
        return false;
    }
}

