package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiTile;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ConfigurableMidiTileSyncPacketHandler {
    public static void handlePacketClient(final ConfigurableMidiTileSyncPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected SyncInstrumentPacket!");
    }

    public static void handlePacketServer(final ConfigurableMidiTileSyncPacket message, ServerPlayer sender) {
        AConfigurableMidiTile tile = (AConfigurableMidiTile)sender.level().getBlockEntity(message.tilePos);

        if(tile != null) {
            ItemStack midiStack = tile.getSourceStack();
            MidiNbtDataUtils.setMidiSource(midiStack, message.midiSource, message.midiSourceName);
            MidiNbtDataUtils.setEnabledChannelsInt(midiStack, message.enabledChannelsInt);
            MidiNbtDataUtils.setFilterInstrument(midiStack, message.instrumentId);
            MidiNbtDataUtils.setFilterNote(midiStack, message.filterNote);
            MidiNbtDataUtils.setFilterOct(midiStack, message.filterOct);
            MidiNbtDataUtils.setInvertNoteOct(midiStack, message.invertNoteOct);
            MidiNbtDataUtils.setInvertSignal(midiStack, message.invertSignal);
            MidiNbtDataUtils.setTriggerNoteStart(midiStack, message.triggerNoteStart);
            MidiNbtDataUtils.setHoldTicks(midiStack, message.holdTicks);
            MidiNbtDataUtils.setBroadcastRange(midiStack, message.broadcastRange);
            MidiNbtDataUtils.setChannelMap(midiStack, message.channelMap);
            tile.setSourceStack(midiStack);
            sender.level().sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), 2);
        }
    }
}
