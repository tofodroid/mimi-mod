package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ATransmitterBroadcastProducer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ServerTransmitterManager;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import io.github.tofodroid.mods.mimi.util.EntityUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class NoteEventPacketHandler {
    public static void handlePacketServer(final NoteEventPacket message, ServerPlayer sender) {
        if(message != null) {
            ServerNoteConsumerManager.handlePacket(message, true, sender.getUUID(), (ServerLevel)sender.level());
            relayToTransmitter(message, sender);
        }
    }

    // Live-played notes only reach nearby players via ServerNoteConsumerManager above. Broadcasted MIDI files
    // already reach transmitter-linked consumers (Relay/Receiver/linked instruments) via ATransmitterBroadcastProducer,
    // but live play never did - mirror what MidiDeviceBroadcastPacketHandler does for external MIDI device input
    // so a held Transmitter also relays notes played via the in-game instrument GUI/keybinds (see issue #184).
    private static void relayToTransmitter(final NoteEventPacket message, ServerPlayer sender) {
        if(EntityUtils.playerHasActiveTransmitter(sender)) {
            ATransmitterBroadcastProducer musicPlayer = ServerTransmitterManager.getTransmitter(sender.getUUID());

            if(musicPlayer != null) {
                musicPlayer.broadcast(new BroadcastEvent(message.type, message.channel, message.data1, message.data2, sender.getUUID(), sender.level().dimension(), message.pos, musicPlayer.getBroadcastRange(), message.noteServerTime));
            }
        }
    }

    public static void handlePacketClient(final NoteEventPacket message) {
        if(MIMIMod.getProxy().isClient()) ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handlePacket(message);
    }
}
