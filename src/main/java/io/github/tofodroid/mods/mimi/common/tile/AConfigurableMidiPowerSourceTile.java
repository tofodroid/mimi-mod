package io.github.tofodroid.mods.mimi.common.tile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.common.block.AConfigurableMidiPowerSourceBlock;
import io.github.tofodroid.mods.mimi.util.ByteUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ByteArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiPowerSourceTile extends AConfigurableMidiNoteResponsiveTile {
    public static final Integer MAX_NOTE_ON_SECONDS = 10;
    
    // Runtime data
    protected Map<Integer, Long> heldNotes = new Int2LongArrayMap();
    protected Map<Integer, Byte> heldVelocities = new Int2ByteArrayMap();
    protected List<Integer> notesToTurnOff = new ArrayList<>();
    protected Byte heldVelocity = 0;
    protected Boolean noteHeld = false;
    protected Integer offCounter = 0;

    // Config data
    protected Boolean analogMode = false;
    protected Boolean triggerHeld = false;
    protected Byte holdTicks = 0;

    public AConfigurableMidiPowerSourceTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiPowerSourceTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
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
        this.analogMode = MidiNbtDataUtils.getAnalogMode(getSourceStack());
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
                this.setPower(this.velocityToPower(this.heldVelocity));
                this.offCounter = 0;
            } else if(state.getValue(AConfigurableMidiPowerSourceBlock.POWER) > 0) {
                if(this.offCounter >= (this.holdTicks-1)) {
                    this.offCounter = 0;
                    this.setPower(0);
                } else {
                    this.offCounter++;
                }
            }
            
            this.noteHeld = tickNotes();
        }
    }

    public Boolean isHeld() {
        return this.triggerHeld && this.noteHeld;
    }

    public Byte getHeldVelocity() {
        return this.heldVelocity;
    }

    public Integer velocityToPower(Byte velocity) {
        int stepAmount = 8;
        int powerLevel = 15;

        if(velocity <= 0) {
            return 0;
        }

        for(int i = 120; i >= 1; i-= stepAmount) {
            if(velocity >= i) {
                return powerLevel;
            }
            powerLevel--;
            stepAmount = stepAmount == 8 ? 9 : 8;
        }
        return 0;
    }

    public Boolean isBlockValid() {
        return getBlockState().getBlock() instanceof AConfigurableMidiPowerSourceBlock;
    }

    public Boolean isPowered() {
        return getBlockState().getValue(AConfigurableMidiPowerSourceBlock.POWER) > 0;
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

    public void setPower(Integer power) {
        if(this.getBlockState().getValue(AConfigurableMidiPowerSourceBlock.POWER) != power) {
            if(!this.isValid()) {
                return;
            }

            this.getLevel().setBlockAndUpdate(
                getBlockPos(), 
                getBlockState()
                    .setValue(AConfigurableMidiPowerSourceBlock.POWER, power)
            );
            
            getLevel().updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    protected Byte calcHeldVelocity() {
        return this.analogMode ? this.heldVelocities.values().stream().max(Byte::compareTo).orElse(ByteUtils.ZERO) : Byte.MAX_VALUE;
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
            if(!this.notesToTurnOff.isEmpty()) {
                for(Integer noteId : notesToRemove) {
                    this.heldNotes.remove(noteId);
                    this.heldVelocities.remove(noteId);
                }
                // Re-calculate max velocity
                this.heldVelocity = this.calcHeldVelocity();
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
        this.heldVelocities.clear();
        this.heldVelocity = 0;
        this.notesToTurnOff.clear();
    }

    protected Boolean hasNotesOn() {
        return !this.heldNotes.isEmpty();
    }
    
    public void onNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId, Long noteTime) {
        Integer noteId = this.getUniqueNoteInt(getNoteGroupKey(channel, instrumentId), note);
        this.heldNotes.put(noteId, Instant.now().toEpochMilli());
        this.heldVelocities.put(noteId, velocity);
        this.heldVelocity = velocity > this.heldVelocity ? velocity : this.heldVelocity;
        this.notesToTurnOff.remove(noteId);
        this.noteHeld = true;
    }

    public void onNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId, Long noteTime) {
        if(this.triggerHeld && this.noteHeld) {
            Integer noteId = this.getUniqueNoteInt(getNoteGroupKey(channel, instrumentId), note);
            this.notesToTurnOff.add(noteId);
        }
    }

    public void onReset(@Nullable Byte channel, @Nullable Byte instrumentId, Long noteTime) {
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