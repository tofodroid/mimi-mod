package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.instruments.InstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;

public class InstrumentItemDataUpdatePacketHandler {
    public static void handlePacket(final InstrumentItemDataUpdatePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected InstrumentDataUpdatePacker!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final InstrumentItemDataUpdatePacket message, ServerPlayerEntity sender) {
        ItemStack instrumentStack = sender.getHeldItem(message.mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND);
        if (instrumentStack != null && !instrumentStack.isEmpty() && instrumentStack.getItem() instanceof ItemInstrument) {
            CompoundNBT comp = instrumentStack.getOrCreateTag();
            
            if(message.midiEnabled) {
                comp.putBoolean(InstrumentDataUtil.MIDI_ENABLED_TAG, message.midiEnabled);
            } else {
                comp.remove(InstrumentDataUtil.MIDI_ENABLED_TAG);
            }

            if(message.acceptedChannelString != null) {
                comp.putString(InstrumentDataUtil.LISTEN_CHANNELS_TAG, message.acceptedChannelString);
            } else {
                comp.remove(InstrumentDataUtil.LISTEN_CHANNELS_TAG);
            }

            if(message.maestroId != null) {
                comp.putUniqueId(InstrumentDataUtil.MAESTRO_TAG, message.maestroId);
            } else {
                comp.remove(InstrumentDataUtil.MAESTRO_TAG);
            }
        }
    }
}
