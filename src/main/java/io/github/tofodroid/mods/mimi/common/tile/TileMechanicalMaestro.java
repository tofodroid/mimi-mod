package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.block.BlockMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.BroadcastConsumerInventoryHolder;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.instrument.InstrumentBroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileMechanicalMaestro extends AContainerTile {
    public static final String REGISTRY_NAME = "mechanicalmaestro";

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
        return new ContainerMechanicalMaestro(id, playerInventory, this);
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
        BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
    }

    @Override
    public void onAddedToLevel() {
        this.refreshMidiReceivers();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.refreshMidiReceivers();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.refreshMidiReceivers();
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();

        if(!this.getLevel().isClientSide()) {
            this.allNotesOff();
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if(!this.getLevel().isClientSide()) {
            this.allNotesOff();
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
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
			ServerNoteConsumerManager.handlePacket(
                MidiNotePacket.createAllNotesOffPacket(instrumentId, this.getUUID(), this.getBlockPos(), null),
                this.getUUID(),
                (ServerLevel)this.getLevel()
            );
		}
    }

    public void refreshMidiReceivers() {
        if(this.hasLevel() && !this.level.isClientSide) {            
            if(this.hasAnInstrument() && this.getBlockState().getValue(BlockMechanicalMaestro.POWERED)) {
                BroadcastConsumerInventoryHolder holder = new BroadcastConsumerInventoryHolder(this.getUUID());
        
                for(int i = 0; i < this.getInstrumentStacks().size(); i++) {
                    ItemStack instrumentStack = this.getInstrumentStacks().get(i);
                    if(instrumentStack != null && MidiNbtDataUtils.getMidiSource(instrumentStack) != null) {
                        holder.putConsumer(i, new InstrumentBroadcastConsumer(
                            this.getBlockPos(),
                            this.getLevel().dimension(),
                            this.getUUID(),
                            instrumentStack,
                            null
                        ));
                    }
                }
                BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
                BroadcastManager.registerConsumers(holder);
            } else {
                this.allNotesOff();
                BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
            }
        }
    }
}
