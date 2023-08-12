package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileReceiver extends APoweredConfigurableMidiTile {
    public TileReceiver(BlockPos pos, BlockState state) {
        super(ModTiles.RECEIVER, pos, state);
    }

    @Override
    public Boolean shouldTriggerFromMidiEvent(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nullable Byte instrumentId) {
        ItemStack sourceStack = getSourceStack();
        if(!sourceStack.isEmpty()) {
            return 
                InstrumentDataUtils.isChannelEnabled(sourceStack, channel) 
                && (note == null || InstrumentDataUtils.isNoteFiltered(sourceStack, note))
                && (sender != null && sender.equals(InstrumentDataUtils.getMidiSource(sourceStack)));
        }
        return false;
    }
    
    @Override
    public Boolean shouldHaveEntity() {
        return true;
    }
}
