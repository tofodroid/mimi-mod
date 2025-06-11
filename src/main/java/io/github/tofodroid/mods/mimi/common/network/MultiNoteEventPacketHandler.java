package io.github.tofodroid.mods.mimi.common.network;

import java.util.List;
import java.util.Map;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.server.level.ServerPlayer;

public class MultiNoteEventPacketHandler {
    public static void handlePacketServer(final MultiNoteEventPacket message, ServerPlayer sender) {
        MIMIMod.LOGGER.warn("Server received unexpected MultiNoteEventPacket!");
    }

    public static void handlePacketClient(final MultiNoteEventPacket message) {
        if(MIMIMod.getProxy().isClient()) {
            for(Map.Entry<Long, List<NoteEventPacket>> packetSet : message.resultPackets.entrySet()) {
                for(NoteEventPacket packet : packetSet.getValue()) {
                    ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handlePacket(packet); 
                }
            }
        }
    }
}
