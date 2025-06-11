package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class NoteEventPacketHandler {
    public static void handlePacketServer(final NoteEventPacket message, ServerPlayer sender) {
        if(message != null) {
            ServerNoteConsumerManager.handlePacket(message, true, sender.getUUID(), (ServerLevel)sender.getLevel());
        }
    }

    public static void handlePacketClient(final NoteEventPacket message) {
        if(MIMIMod.getProxy().isClient()) ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handlePacket(message); 
    }
}
