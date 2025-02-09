package io.github.tofodroid.mods.mimi.common.network;

import java.util.List;
import java.util.Map;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.server.level.ServerPlayer;

public class MultiMidiNotePacketHandler {
    public static void handlePacketServer(final MultiMidiNotePacket message, ServerPlayer sender) {
        MIMIMod.LOGGER.warn("Server received unexpected MultiMidiNotePacket!");
    }

    public static void handlePacketClient(final MultiMidiNotePacket message) {
        if(MIMIMod.getProxy().isClient()) {
            for(Map.Entry<Long, List<MidiNotePacket>> packetSet : message.resultPackets.entrySet()) {
                for(MidiNotePacket packet : packetSet.getValue()) {
                    ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handlePacket(packet); 
                }
            }
        }
    }
}
