package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ASwitchboardContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SwitchboardStackUpdatePacketHandler {
    public static void handlePacket(final SwitchboardStackUpdatePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client received unexpected SwitchboardStackUpdatePacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final SwitchboardStackUpdatePacket message, ServerPlayer sender) {
        if(sender.containerMenu != null) {
            if(sender.containerMenu instanceof ASwitchboardContainer) {
                ((ASwitchboardContainer)sender.containerMenu).updateSelectedSwitchboard(sender, message.midiSource, message.midiSourceName, message.filterOct, message.filterNote, message.invertNoteOct, message.enabledChannelsString, message.instrumentId, message.invertInstrument, message.sysInput, message.publicBroadcast, message.broadcastNote, message.volume);
            }
        }
    }
}
