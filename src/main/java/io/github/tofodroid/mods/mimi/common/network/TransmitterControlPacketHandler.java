package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class TransmitterControlPacketHandler {
    public static void handlePacket(final TransmitterControlPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final TransmitterControlPacket message, ServerPlayer sender) {
        switch(message.control) {
            case PLAY:
                if(ServerMusicPlayerMidiManager.hasTransmitter(sender.getUUID())) {
                    ServerMusicPlayerMidiManager.playTransmitter(sender.getUUID());
                } else if(message.data.isPresent()) {
                    ServerMusicPlayerMidiManager.createOrReplaceTransmitter(sender, MidiFileCacheManager.getSequenceByIndex(message.data.get()));
                    ServerMusicPlayerMidiManager.playTransmitter(sender.getUUID());
                }
                break;
            case PAUSE:
                ServerMusicPlayerMidiManager.pauseTransmitter(sender.getUUID());
                break;
            case STOP:
                ServerMusicPlayerMidiManager.stopTransmitter(sender.getUUID());
                break;
            case SELECT:
                ServerMusicPlayerMidiManager.createOrReplaceTransmitter(sender, MidiFileCacheManager.getSequenceByIndex(message.data.get()));
            case SEEK:
                ServerMusicPlayerMidiManager.seekTransmitter(sender.getUUID(), message.data.get());
                break;
            default:
                break;
        }
        sendStatusPacket(sender);
    }

    public static void sendStatusPacket(ServerPlayer sender) {
        ServerMusicPlayerStatusPacketHandler.handlePacketServer(sender);
    }
    
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("resource")
    public static void handlePacketClient(final TransmitterControlPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected TransmitterControlPacket!");
    }
}
