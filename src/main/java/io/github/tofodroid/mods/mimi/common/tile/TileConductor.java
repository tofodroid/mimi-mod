package io.github.tofodroid.mods.mimi.common.tile;

import java.util.Set;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.server.midi.receiver.ServerMusicReceiverManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileConductor extends AConfigurableMidiTile {
    public static final String REGISTRY_NAME = "conductor";

    public TileConductor(BlockPos pos, BlockState state) {
        super(ModTiles.CONDUCTOR, pos, state);
    }

    protected void sendPacket(Boolean stop) {
        Level level = getLevel();
        if(level instanceof ServerLevel) {
            ItemStack sourceStack = getSourceStack();
            Set<Byte> channels = MidiNbtDataUtils.getEnabledChannelsSet(sourceStack);

            if(channels.size() > 0) {
                Byte note = MidiNbtDataUtils.getBroadcastNote(sourceStack);
                Byte velocity = stop ? -1 : Byte.MAX_VALUE;
                
                for(Byte channel : channels) {
                    ServerMusicReceiverManager.handlePacket(
                        TransmitterNoteEvent.createNoteEvent(channel, note, velocity), 
                        getUUID(),
                        getBlockPos(), 
                        (ServerLevel)level
                    );
                }
            }
        }        
    }

    @Override
    public void setSourceStack(ItemStack stack) {
        if(stack.getItem().getClass().equals(this.getBlockState().getBlock().asItem().getClass())) {
            this.stopNote();
            this.setItem(SOURCE_STACK_SLOT, stack);
        }
    }

    public void startNote() {
        this.sendPacket(false);
    }

    public void stopNote() {
        this.sendPacket(true);
    }
}
