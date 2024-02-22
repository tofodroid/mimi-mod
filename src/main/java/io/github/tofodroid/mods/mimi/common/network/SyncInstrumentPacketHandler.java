package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SyncInstrumentPacketHandler {
    public static void handlePacketClient(final SyncInstrumentPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected SyncInstrumentPacket!");
    }
    
    public static void handlePacketServer(final SyncInstrumentPacket message, ServerPlayer sender) {
        ItemStack instrumentStack;
        TileInstrument tileInstrument = null;
        if(message.handIn != null) {
            instrumentStack = sender.getItemInHand(message.handIn);
        } else {
            tileInstrument = BlockInstrument.getTileInstrumentForEntity(sender);
            instrumentStack = tileInstrument.getInstrumentStack();
        }
        
        MidiNbtDataUtils.setMidiSource(instrumentStack, message.midiSource, message.midiSourceName);
        MidiNbtDataUtils.setEnabledChannelsInt(instrumentStack, message.enabledChannelsInt);
        MidiNbtDataUtils.setSysInput(instrumentStack, message.sysInput);
        MidiNbtDataUtils.setInstrumentVolume(instrumentStack, message.volume);
        
        if(tileInstrument == null) {
            sender.setItemInHand(message.handIn, instrumentStack);
        } else {
            tileInstrument.setInstrumentStack(instrumentStack);
            sender.getLevel().sendBlockUpdated(tileInstrument.getBlockPos(), tileInstrument.getBlockState(), tileInstrument.getBlockState(), 2);
        }
    }
}
