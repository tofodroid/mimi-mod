package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.server.midi.receiver.ServerMusicReceiverManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileReceiver extends AConfigurableMidiPowerSourceTile {
    public static final String REGISTRY_NAME = "receiver";

    public TileReceiver(BlockPos pos, BlockState state) {
        super(ModTiles.RECEIVER, pos, state);
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state) {
        super.tick(world, pos, state);
        ServerMusicReceiverManager.loadConfigurableMidiNoteResponsiveTileReceiver(this);
    }

    @Override
    public Boolean shouldTriggerFromNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        ItemStack sourceStack = getSourceStack();
        if(!sourceStack.isEmpty()) {
            return 
                MidiNbtDataUtils.isChannelEnabled(this.enabledChannels, channel)
                && (note == null || MidiNbtDataUtils.isNoteFiltered(filterNote, filterOct, invertFilterNoteOct, note));
        }
        return false;
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
        return channel;
    }
}
