package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import io.github.tofodroid.mods.mimi.server.midi.receiver.ServerMusicReceiverManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileMechanicalMaestro extends AContainerTile {
    public static final UUID MECH_SOURCE_ID = new UUID(0,3);

    private UUID id;

    public TileMechanicalMaestro(BlockPos pos, BlockState state) {
        super(ModTiles.MECHANICALMAESTRO, pos, state, 3);
    }
    
    public UUID getUUID() {
        if(this.id == null) {
            String idString = getClass().getSimpleName() + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
            this.id = UUID.nameUUIDFromBytes(idString.getBytes());
        }
        return this.id;
    }

    @Override
    public Component getDefaultName() {
		return Component.translatable(this.getBlockState().getBlock().asItem().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new ContainerMechanicalMaestro(id, playerInventory, this.getBlockPos());
    }

    @Override
    public void setItem(int i, ItemStack item) {
        ItemStack oldStack = this.getItem(i);
        
        if(!oldStack.isEmpty()) {
            this.allNotesOff(((IInstrumentItem)oldStack.getItem()).getInstrumentId());
        }

        super.setItem(i, item);
        this.refreshMidiReceivers();
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        ItemStack result = super.removeItem(i, count);
        this.allNotesOff(((IInstrumentItem)result.getItem()).getInstrumentId());
        this.refreshMidiReceivers();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack result = super.removeItemNoUpdate(i);
        this.allNotesOff(((IInstrumentItem)result.getItem()).getInstrumentId());
        this.refreshMidiReceivers();
        return result;
    }
    
    @Override
    public void clearContent() {
        this.allNotesOff();
        super.clearContent();
        ServerMusicReceiverManager.removeReceivers(this.getUUID());
    }

    @Override
    public void onAddedToLevel() {
        this.refreshMidiReceivers();
    }

    @SuppressWarnings("null")
    public void refreshMidiReceivers() {
        if(this.hasLevel() && !this.level.isClientSide) {
            MIMIMod.LOGGER.info("Mech Maestro refresh");
            if(this.hasAnInstrument() && this.getBlockState().getValue(BlockMechanicalMaestro.POWERED)) {
                ServerMusicReceiverManager.loadMechanicalMaestroInstrumentReceivers(this);
            } else {
                ServerMusicReceiverManager.removeReceivers(this.getUUID());
            }
        }
    }

    public List<ItemStack> getInstrumentStacks() {
        return this.getItems().stream().filter(stack -> stack.getItem() instanceof IInstrumentItem).collect(Collectors.toList());
    }

    public Boolean hasAnInstrument() {
        return this.getItems().stream().anyMatch(i -> i.getItem() instanceof IInstrumentItem);
    }

    public void allNotesOff() {
        this.getItems().stream().forEach(i -> {
            if(i.getItem() instanceof IInstrumentItem) {
                this.allNotesOff(((IInstrumentItem)i.getItem()).getInstrumentId());
            }
        });
    }
    
    public void allNotesOff(Byte instrumentId) {
		if(instrumentId != null && this.getLevel() instanceof ServerLevel) {
			MidiNotePacketHandler.handlePacketServer(
                MidiNotePacket.createAllNotesOffPacket(instrumentId, TileMechanicalMaestro.MECH_SOURCE_ID, this.getBlockPos()),
                (ServerLevel)this.getLevel(),
                null
            );
		}
    }
}
