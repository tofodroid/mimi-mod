package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
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
    public Boolean shouldTriggerFromMidiEvent(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        ItemStack sourceStack = getSourceStack();

        if(!sourceStack.isEmpty()) {
            return 
                (note == null || InstrumentDataUtils.isNoteFiltered(filteredNotes, invertFilterNoteOct, note)) && 
                (instrumentId == null || InstrumentDataUtils.isInstrumentFiltered(filteredInstrument, invertFilterInstrument, instrumentId))
            ;
        }

        return false;
    }
}
