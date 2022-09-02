package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import javax.sound.midi.Sequence;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.client.gui.GuiBroadcasterContainerScreen;
import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitterContainerScreen;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiStatus.STATUS_CODE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerMidiInfoPacketHandler {
    public static void handlePacket(final ServerMidiInfoPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final ServerMidiInfoPacket message, ServerPlayer sender) {
        Pair<Sequence,STATUS_CODE> result = MidiFileCacheManager.getOrCreateCachedSequence(message.midiUrl);
        ServerMidiInfoPacket resultPacket;

        if(result.getRight() == null && result.getLeft() != null) {
            MidiFileInfo info = MidiFileInfo.fromSequence(message.midiUrl, result.getLeft());
            resultPacket = new ServerMidiInfoPacket(STATUS_CODE.SUCCESS, info.byteChannelMapping, info.songLength);
        } else {
            resultPacket = new ServerMidiInfoPacket(result.getRight(), null, null);
        }
        
        NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), resultPacket);
    }
    
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("resource")
    public static void handlePacketClient(final ServerMidiInfoPacket message) {
        Screen screen = Minecraft.getInstance().screen;
        if(screen != null && screen instanceof GuiBroadcasterContainerScreen) {
            ((GuiBroadcasterContainerScreen)screen).handleMidiInfoPacket(message);
        } else if(screen != null && screen instanceof GuiTransmitterContainerScreen) {
            ((GuiTransmitterContainerScreen)screen).handleMidiInfoPacket(message);
        }
    }
}
