package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.block.AConfigurableMidiPowerSourceBlock;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils.TriggerMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiPowerSourceTile extends AConfigurableMidiNoteResponsiveTile {
    public static final Integer MAX_NOTE_ON_TICKS = 200;
    
    private Map<Byte, Map<Byte, Integer>> heldNotes = new HashMap<>();
    private Integer offCounter = 0;
    private TriggerMode triggerMode = TriggerMode.NOTE_ON;
    private Byte holdTicks = 0;

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
    protected void cacheMidiSettings() {
        super.cacheMidiSettings();
        this.setInverted(MidiNbtDataUtils.getInvertSignal(getSourceStack()));
        this.triggerMode = MidiNbtDataUtils.getTriggerMode(getSourceStack());
        this.holdTicks = MidiNbtDataUtils.getHoldTicks(getSourceStack());
        this.clearNotes();
    }

    @Override
    public void execServerTick(ServerLevel world, BlockPos pos, BlockState state) {
        if(this.isBlockValid()) {
            Boolean shouldBePowered = false;
            
            if(this.triggerMode == TriggerMode.NOTE_HELD) {
                shouldBePowered = tickNotes();
            }

            if(shouldBePowered) {
                this.setPowered(true);
                this.offCounter = 0;
            } else if(state.getValue(AConfigurableMidiPowerSourceBlock.POWERED)) {
                if(this.offCounter >= this.holdTicks || this.offCounter >= MAX_NOTE_ON_TICKS) {
                    this.offCounter = 0;
                    this.setPowered(false);
                }
                this.offCounter++;
            }
        }
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
            this.getLevel().setBlockAndUpdate(
                getBlockPos(), 
                getBlockState()
                    .setValue(AConfigurableMidiPowerSourceBlock.POWERED, powered)
            );
            
            getLevel().updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    protected Boolean tickNotes() {
        List<Byte> groupsToRemove = new ArrayList<>();

        for(Byte groupId : this.heldNotes.keySet()) {
            Map<Byte, Integer> group = this.heldNotes.get(groupId);
            List<Byte> notesToRemove = new ArrayList<>();
        
            for(Map.Entry<Byte, Integer> note : group.entrySet()) {
                note.setValue(note.getValue()+1);

                if(note.getValue() >= MAX_NOTE_ON_TICKS) {
                    notesToRemove.add(note.getKey());
                }
            }

            for(Byte note : notesToRemove) {
                group.remove(note);
            }

            if(group.isEmpty()) {
                groupsToRemove.add(groupId);
            }
        }

        for(Byte groupId : groupsToRemove) {
            this.heldNotes.remove(groupId);
        }

        return !this.heldNotes.isEmpty();
    }

    protected void clearNotes() {
        this.heldNotes.clear();
    }

    protected Boolean hasNotesOn() {
        return !this.heldNotes.isEmpty();
    }
    
    public void onNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        if(this.triggerMode == TriggerMode.NOTE_HELD) {
            Byte groupKey = getNoteGroupKey(channel, instrumentId);
            this.heldNotes.computeIfAbsent(groupKey, (key) -> new HashMap<>()).put(note, 0);
        } else if(this.triggerMode == TriggerMode.NOTE_ON || this.triggerMode == TriggerMode.NOTE_ON_OFF) {
            this.setPowered(true);
        }
    }

    public void onNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        if(this.triggerMode == TriggerMode.NOTE_HELD) {
            Byte groupKey = getNoteGroupKey(channel, instrumentId);
            Map<Byte, Integer> groupMap = this.heldNotes.computeIfAbsent(groupKey, (key) -> new HashMap<>());
            groupMap.remove(note);

            if(groupMap.isEmpty()) {
                this.heldNotes.remove(groupKey);
            }
        } else if(this.triggerMode == TriggerMode.NOTE_OFF || this.triggerMode == TriggerMode.NOTE_ON_OFF) {
            this.setPowered(true);
        }
    }

    public void onAllNotesOff(@Nullable Byte channel, @Nullable Byte instrumentId) {
        if(this.triggerMode == TriggerMode.NOTE_HELD) {
            Byte groupKey = getNoteGroupKey(channel, instrumentId);

            if(groupKey == null) {
                this.heldNotes.clear();
            } else {
                this.heldNotes.remove(groupKey);
            }
        } else if(this.triggerMode == TriggerMode.NOTE_OFF || this.triggerMode == TriggerMode.NOTE_ON_OFF) {
            this.setPowered(true);
        }
    }

    public abstract Byte getNoteGroupKey(@Nullable Byte channel, @Nullable Byte instrumentId);
}
