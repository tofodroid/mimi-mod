package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerMidiListPacketHandler {
    public static void handlePacket(final ServerMidiListPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final ServerMidiListPacket message, ServerPlayer sender) {
        List<MidiFileInfo> midiList = new ArrayList<>();
        NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new ServerMidiListPacket(midiList));
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void handlePacketClient(final ServerMidiListPacket message) {
        ((ClientProxy)MIMIMod.proxy).getMidiInput().enderTransmitterManager.finishLoadServerSongsFromRemote(message);
    }
}
