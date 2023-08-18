package io.github.tofodroid.mods.mimi.common.tile;

import java.util.Set;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacketHandler;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileConductor extends AConfigurableMidiTile {
    public TileConductor(BlockPos pos, BlockState state) {
        super(ModTiles.CONDUCTOR, pos, state);
    }

    protected void sendPacket(Boolean stop) {
        Level level = getLevel();
        if(level instanceof ServerLevel) {
            ItemStack sourceStack = getSourceStack();
            Set<Byte> channels = InstrumentDataUtils.getEnabledChannelsSet(sourceStack);

            if(channels.size() > 0) {
                Byte note = InstrumentDataUtils.getBroadcastNote(sourceStack);
                Byte velocity = stop ? -1 : Byte.MAX_VALUE;
                
                for(Byte channel : channels) {
                    TransmitterNotePacketHandler.handlePacketServer(
                        TransmitterNotePacket.createNotePacket(channel, note, velocity), 
                        getBlockPos(), 
                        (ServerLevel)level,
                        getUniqueId()
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

    public UUID getUniqueId() {
        String posString = "con" + this.getBlockPos().getX() + this.getBlockPos().getY() + this.getBlockPos().getZ();
        return UUID.nameUUIDFromBytes(posString.getBytes());
    }

    public void startNote() {
        this.sendPacket(false);
    }

    public void stopNote() {
        this.sendPacket(true);
    }
}
