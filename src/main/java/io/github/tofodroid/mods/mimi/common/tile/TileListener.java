package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileListener extends AConfigurableMidiPowerSourceTile {
    public static final String REGISTRY_NAME = "listener";
    private static final Integer UPDATE_EVERY_TICKS = 8;

    private Integer updateTickCount = 0;

    public TileListener(BlockPos pos, BlockState state) {
        super(ModTiles.LISTENER, pos, state);
    }

    @Override
    protected void tick(Level world, BlockPos pos, BlockState state) {
        super.tick(world, pos, state);

        if(!isValid()) {
            return;
        }

        if(this.updateTickCount >= UPDATE_EVERY_TICKS) {
            this.updateTickCount = 0;

            if(!world.isClientSide && !this.isRemoved()) {
                EntityNoteResponsiveTile.create(world, pos);
            }
        } else {
            this.updateTickCount++;
        }
    }
    
    @Override
    public Boolean shouldTriggerFromNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        return (note == null || MidiNbtDataUtils.isNoteFiltered(filterNote, filterOctMin, filterOctMax, invertFilterNoteOct, note)) && 
            (instrumentId == null || MidiNbtDataUtils.isInstrumentFiltered(filteredInstrument, invertFilterInstrument, instrumentId));
    }
    
    @Override
    public Boolean shouldTriggerFromNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        return this.shouldTriggerFromNoteOn(channel, note, velocity, instrumentId);
    }

    @Override
    public Boolean shouldTriggerFromAllNotesOff(Byte channel, Byte instrumentId) {
        return this.shouldTriggerFromNoteOn(channel, null, null, instrumentId);
    }

    @Override
    public Byte getNoteGroupKey(Byte channel, Byte instrumentId) {
        return instrumentId;
    }
}
