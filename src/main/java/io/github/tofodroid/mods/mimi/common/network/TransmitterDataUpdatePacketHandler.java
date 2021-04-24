package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class TransmitterDataUpdatePacketHandler {
    public static void handlePacket(final TransmitterDataUpdatePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected TransmitterDataUpdatePacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final TransmitterDataUpdatePacket message, ServerPlayerEntity sender) {
        ItemStack transmitterStack = sender.getHeldItemMainhand();
        if (transmitterStack != null && !transmitterStack.isEmpty() && transmitterStack.getItem() instanceof ItemTransmitter) {
            CompoundNBT comp = transmitterStack.getOrCreateTag();
            
            if(message.enabled) {
                comp.putBoolean(ItemTransmitter.ENABLED_TAG, message.enabled);
            } else {
                comp.remove(ItemTransmitter.ENABLED_TAG);
            }
            
            if(message.mode) {
                comp.putBoolean(ItemTransmitter.MODE_TAG, message.mode);
            } else {
                comp.remove(ItemTransmitter.MODE_TAG);
            }
        }
    }
}
