package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileMechanicalMaestro extends AContainerTile implements INoteResponsiveTile<TileMechanicalMaestro> {
    public static final UUID MECH_UUID = new UUID(0,3);

    private Integer updateTickCount;

    public TileMechanicalMaestro(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 3);
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
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        ItemStack result = super.removeItem(i, count);
        this.allNotesOff(((IInstrumentItem)result.getItem()).getInstrumentId());
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack result = super.removeItemNoUpdate(i);
        this.allNotesOff(((IInstrumentItem)result.getItem()).getInstrumentId());
        return result;
    }
    
    @Override
    public void clearContent() {
        this.allNotesOff();
        super.clearContent();
    }
    
    @Override
    @SuppressWarnings("null")
    public void tick(Level world, BlockPos pos, BlockState state, TileMechanicalMaestro self) {
        if(getTickCount() >= UPDATE_EVERY_TICKS) {
            setTickCount(0);
            if(this.hasLevel() && !this.level.isClientSide) {
                if(this.shouldHaveEntity() && !this.isRemoved()) {
                    EntityNoteResponsiveTile.create(this.level, this.getBlockPos());
                } else if(EntityNoteResponsiveTile.entityExists(this.level, Double.valueOf(this.getBlockPos().getX()), Double.valueOf(this.getBlockPos().getY()), Double.valueOf(this.getBlockPos().getZ()))) {
                    EntityNoteResponsiveTile.remove(this.level, this.getBlockPos());
                    this.allNotesOff();
                }
            }
        } else {
            setTickCount(getTickCount()+1);
        }        
    }

    @Override
    @SuppressWarnings("null")
    public Boolean shouldHaveEntity() {
        return this.hasAnInstrument() && this.level.hasNeighborSignal(this.getBlockPos());
    }

    @Override
    public Boolean onTrigger(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        List<MidiNotePacket> packets = new ArrayList<>();

        if(level instanceof ServerLevel) {
            this.getItems().stream().forEach(i -> {
                if(InstrumentDataUtils.shouldInstrumentRespondToMessage(i, sender, channel)) {
                    packets.add(MidiNotePacket.createNotePacket(
                        note, 
                        InstrumentDataUtils.applyVolume(i, velocity), 
                        InstrumentDataUtils.getInstrumentId(i), 
                        MECH_UUID, 
                        worldPosition
                    ));
                }
            });

            if(packets.size() > 0) {
                MidiNotePacketHandler.handlePacketsServer(packets, (ServerLevel)level, null);
                return true;
            }
        }
    
        return false;
    }

    @Override
    public Boolean shouldTriggerFromMidiEvent(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        // No-op, push to onTrigger
        return true;
    }

    @Override
    public Integer getTickCount() {
        return updateTickCount;
    }

    @Override
    public void setTickCount(Integer count) {
        this.updateTickCount = count;
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
			MidiNotePacketHandler.handlePacketsServer(
                Arrays.asList(MidiNotePacket.createAllNotesOffPacket(instrumentId, TileMechanicalMaestro.MECH_UUID, this.getBlockPos())),
                (ServerLevel)this.getLevel(),
                null
            );
		}
    }
}
