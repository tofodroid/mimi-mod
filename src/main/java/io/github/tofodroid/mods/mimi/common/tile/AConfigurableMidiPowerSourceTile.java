package io.github.tofodroid.mods.mimi.common.tile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.block.AConfigurableMidiPowerSourceBlock;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiPowerSourceTile extends AConfigurableMidiNoteResponsiveTile {
    public static final Integer MAX_NOTE_ON_SECONDS = 10;
    
    // Runtime data
    protected Map<Integer, Long> heldNotes = new Int2LongArrayMap();
    protected List<Integer> notesToTurnOff = new ArrayList<>();
    protected Boolean noteHeld = false;
    protected Integer offCounter = 0;

    // Config data
    protected Boolean triggerHeld = false;
    protected Byte holdTicks = 0;

    public AConfigurableMidiPowerSourceTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiPowerSourceTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public static void doTick(Level world, BlockPos pos, BlockState state, AConfigurableMidiPowerSourceTile self) {
        self.tick(world, pos, state);
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();

        if(!this.getLevel().isClientSide()) {
            this.offCounter = 0;
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    
        if(!this.getLevel().isClientSide()) {
            this.offCounter = 0;
        }
    }
    
    @Override
    protected void cacheMidiSettings() {
        super.cacheMidiSettings();
        this.setInverted(MidiNbtDataUtils.getInvertSignal(getSourceStack()));
        this.triggerHeld = !MidiNbtDataUtils.getTriggerNoteStart(getSourceStack());
        this.holdTicks = MidiNbtDataUtils.getHoldTicks(getSourceStack());
        this.noteHeld = false;
        this.clearNotes();
    }

    @Override
    public void execServerTick(ServerLevel world, BlockPos pos, BlockState state) {
        if(this.isBlockValid()) {
            Boolean shouldBePowered = this.noteHeld;

            if(shouldBePowered) {
                this.setPowered(true);
                this.offCounter = 0;
            } else if(state.getValue(AConfigurableMidiPowerSourceBlock.POWERED)) {
                if(this.offCounter >= (this.holdTicks-1)) {
                    this.offCounter = 0;
                    this.setPowered(false);
                } else {
                    this.offCounter++;
                }
            }
            
            this.noteHeld = tickNotes();
        }
    }

    @Override
    public Boolean shouldTriggerFromNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        return this.triggerHeld && this.noteHeld && this.shouldTriggerFromNoteOn(channel, note, velocity, instrumentId);
    }

    @Override
    public Boolean shouldTriggerFromAllNotesOff(Byte channel, Byte instrumentId) {
        return this.triggerHeld && this.noteHeld && this.shouldTriggerFromNoteOn(channel, null, null, instrumentId);
    }

    public Boolean isBlockValid() {
        return getBlockState().getBlock() instanceof AConfigurableMidiPowerSourceBlock;
    }

    public Boolean isPowered() {
        return getBlockState().getValue(AConfigurableMidiPowerSourceBlock.POWERED);
    }

    public Boolean isInverted() {
        return getBlockState().getValue(AConfigurableMidiPowerSourceBlock.INVERTED);
    }

    public Boolean stackIsInverted() {
        return MidiNbtDataUtils.getInvertSignal(getSourceStack());
    }

    public void setInverted(Boolean inverted) {
        if(this.getBlockState().getValue(AConfigurableMidiPowerSourceBlock.INVERTED) != inverted) {
            this.getLevel().setBlockAndUpdate(
                getBlockPos(), 
                getBlockState()
                    .setValue(AConfigurableMidiPowerSourceBlock.INVERTED, inverted)
            );
            
            getLevel().updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public void setPowered(Boolean powered) {
        if(this.getBlockState().getValue(AConfigurableMidiPowerSourceBlock.POWERED) != powered) {
            if(!this.isValid()) {
                return;
            }

            this.getLevel().setBlockAndUpdate(
                getBlockPos(), 
                getBlockState()
                    .setValue(AConfigurableMidiPowerSourceBlock.POWERED, powered)
            );
            
            getLevel().updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    protected Integer getUniqueNoteInt(Byte group, Byte note) {
        return ((group)*128 + note);
    }

    protected Boolean tickNotes() {
        if(this.triggerHeld) {
            List<Integer> notesToRemove = new ArrayList<>();
            Long nowTime = Instant.now().toEpochMilli();

            // Find notes that were turned off or held for longer than MAX_NOTE_ON_SECONDS and time out
            for(Integer noteId : this.heldNotes.keySet()) {
                if(this.notesToTurnOff.contains(noteId) || nowTime - this.heldNotes.get(noteId) >= MAX_NOTE_ON_SECONDS * 1000) {
                    notesToRemove.add(noteId);
                }
            }

            // Remove identified notes
            for(Integer noteId : notesToRemove) {
                this.heldNotes.remove(noteId);
            }

            this.notesToTurnOff.clear();
            return !this.heldNotes.isEmpty();
        } else {
            this.clearNotes();
            return false;
        }
    }

    protected void clearNotes() {
        this.heldNotes.clear();
        this.notesToTurnOff.clear();
    }

    protected Boolean hasNotesOn() {
        return !this.heldNotes.isEmpty();
    }
    
    public void onNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId, Long noteTime) {
        Integer noteId = this.getUniqueNoteInt(getNoteGroupKey(channel, instrumentId), note);
        this.heldNotes.put(noteId, Instant.now().toEpochMilli());
        this.noteHeld = true;
    }

    public void onNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        if(this.triggerHeld && this.noteHeld) {
            Integer noteId = this.getUniqueNoteInt(getNoteGroupKey(channel, instrumentId), note);
            this.notesToTurnOff.add(noteId);
        }
    }

    public void onAllNotesOff(@Nullable Byte channel, @Nullable Byte instrumentId) {
        if(this.triggerHeld) {
            Byte groupKey = getNoteGroupKey(channel, instrumentId);

            if(groupKey == null || (channel != null && channel == BroadcastEvent.ALL_CHANNELS)) {
                this.clearNotes();
            } else if(groupKey != null) {
                for(Integer noteId : this.heldNotes.keySet()) {
                    if((noteId / 128) == groupKey) {
                        this.notesToTurnOff.add(noteId);
                    }
                }
            }
        }
    }

    public abstract Byte getNoteGroupKey(@Nullable Byte channel, @Nullable Byte instrumentId);
}