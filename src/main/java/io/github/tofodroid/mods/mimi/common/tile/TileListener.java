package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileListener extends AConfigurableMidiPowerSourceTile {
    public TileListener(BlockPos pos, BlockState state) {
        super(ModTiles.LISTENER, pos, state);
    }
    
    @Override
    public Boolean shouldTriggerFromMidiEvent(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        ItemStack sourceStack = getSourceStack();

        if(!sourceStack.isEmpty()) {
            return 
                (note == null || InstrumentDataUtils.isNoteFiltered(sourceStack, note)) && 
                (instrumentId == null || InstrumentDataUtils.isInstrumentFiltered(sourceStack, instrumentId))
            ;
        }

        return false;
    }
}
