package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitterBlock;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.AServerMusicTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
public class ServerMusicPlayerStatusPacketHandler {
    public static void handlePacketServer(ServerMusicPlayerStatusPacket request, ServerPlayer sender) {
        AServerMusicTransmitter player = ServerMusicTransmitterManager.getMusicPlayer(request.musicPlayerId);
        
        if(player != null) {
            NetworkProxy.sendToPlayer(sender, player.getStatus());
        }
    }

    @SuppressWarnings({"resource"})
    public static void handlePacketClient(final ServerMusicPlayerStatusPacket message) {
        if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof GuiTransmitterBlock && message.musicPlayerId.equals(((GuiTransmitterBlock)Minecraft.getInstance().screen).getMusicPlayerId())) {
            ((GuiTransmitterBlock)Minecraft.getInstance().screen).handleMusicPlayerStatusPacket(message);
        }

        if(message.musicPlayerId.equals(Minecraft.getInstance().player.getUUID())) {
            ((ClientProxy)MIMIMod.getProxy()).getMidiData().setPlayerStatusPakcet(message);
        }
    }
}
