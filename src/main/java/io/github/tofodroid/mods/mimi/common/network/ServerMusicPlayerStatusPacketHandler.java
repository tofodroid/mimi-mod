package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.MusicPlayerMidiHandler;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerMusicPlayerStatusPacketHandler {
    public static void handlePacket(final ServerMusicPlayerStatusPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(ServerPlayer sender) {
        ServerMusicPlayerStatusPacket returnPacket = ServerMusicPlayerStatusPacket.requestPacket();

        MusicPlayerMidiHandler playerHandler = ServerMusicPlayerMidiManager.getTransmitterHandler(sender.getUUID());
            
        if(playerHandler != null) {
            returnPacket = new ServerMusicPlayerStatusPacket(
                playerHandler.getPositionSeconds(),
                playerHandler.isPlaying(),
                playerHandler.isComplete()
            );
        }

        NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), returnPacket);
    }
    
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("resource")
    public static void handlePacketClient(final ServerMusicPlayerStatusPacket message) {
        ((ClientProxy)MIMIMod.proxy).getMidiInput().enderTransmitterManager.finishRefreshMediaStatus(message);
    }
}
