package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.AServerMusicTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ClientMidiListPacketHandler {
    public static void handlePacket(final ClientMidiListPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacketClient(final ClientMidiListPacket message) {
        MIMIMod.proxy.clientMidiFiles().refresh();
        NetworkManager.INFO_CHANNEL.sendToServer(new ClientMidiListPacket(false, MIMIMod.proxy.clientMidiFiles().getSortedSongInfos()));
    }
    
    public static void handlePacketServer(final ClientMidiListPacket message, ServerPlayer sender) {
        ServerMidiManager.setCacheInfosForSource(sender.getUUID(), message.infos);
        AServerMusicTransmitter player = ServerMusicTransmitterManager.getMusicPlayer(sender.getUUID());

        if(message.doUpdateServerFileList) {
            MIMIMod.proxy.serverMidiFiles().refresh();
        }

        if(player != null) {
            player.refreshSongs();
        } else {
            ServerMusicTransmitterManager.createTransmitter(sender);
            player = ServerMusicTransmitterManager.getMusicPlayer(sender.getUUID());
        }

        NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new ServerMusicPlayerSongListPacket(sender.getUUID(), player.getCurrentSongsSorted(), player.getCurrentFavoriteIndicies()));
    }
}
