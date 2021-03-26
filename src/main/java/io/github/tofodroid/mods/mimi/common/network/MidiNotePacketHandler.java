package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

public class MidiNotePacketHandler {
    public static void handlePacket(final MidiNoteOnPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message, ctx.get().getSender()));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacket(final MidiNoteOffPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message, ctx.get().getSender()));
        }
        
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacketServer(final MidiNoteOnPacket message, ServerPlayerEntity sender) {
        PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(() -> 
            new PacketDistributor.TargetPoint(message.pos.getX(), message.pos.getY(), message.pos.getZ(), 64.0D, sender.getServerWorld().getDimensionKey())
        );
        NetworkManager.NET_CHANNEL.send(target, message);
    }
    
    public static void handlePacketServer(final MidiNoteOffPacket message, ServerPlayerEntity sender) {
        PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(() -> 
            new PacketDistributor.TargetPoint(sender.getPosX(), sender.getPosY(), sender.getPosZ(), 68.0D, sender.getServerWorld().getDimensionKey())
        );
        NetworkManager.NET_CHANNEL.send(target, message);
    }

    public static void handlePacketClient(final MidiNoteOnPacket message, ServerPlayerEntity sender) {
        MIMIMod.proxy.getMidiSynth().handleNoteOn(message);
    }
    
    public static void handlePacketClient(final MidiNoteOffPacket message, ServerPlayerEntity sender) {
        MIMIMod.proxy.getMidiSynth().handleNoteOff(message);
    }
}
