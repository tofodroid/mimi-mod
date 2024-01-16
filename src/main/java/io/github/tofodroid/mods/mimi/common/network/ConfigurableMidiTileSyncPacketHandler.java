package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiTile;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
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
            InstrumentDataUtils.setMidiSource(midiStack, message.midiSource, message.midiSourceName);
            InstrumentDataUtils.setEnabledChannelsInt(midiStack, message.enabledChannelsInt);
            InstrumentDataUtils.setFilterInstrument(midiStack, message.instrumentId);
            InstrumentDataUtils.setFilterNote(midiStack, message.filterNote);
            InstrumentDataUtils.setFilterOct(midiStack, message.filterOct);
            InstrumentDataUtils.setInvertInstrument(midiStack, message.invertInstrument);
            InstrumentDataUtils.setInvertNoteOct(midiStack, message.invertNoteOct);
            tile.setSourceStack(midiStack);
            sender.level().sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), 2);
        }
    }
}
