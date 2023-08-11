package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SyncInstrumentPacketHandler {
    public static void handlePacket(final SyncInstrumentPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client received unexpected SyncInstrumentPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final SyncInstrumentPacket message, ServerPlayer sender) {
        ItemStack instrumentStack;
        TileInstrument tileInstrument = null;
        if(message.handIn != null) {
            instrumentStack = sender.getItemInHand(message.handIn);
        } else {
            tileInstrument = BlockInstrument.getTileInstrumentForEntity(sender);
            instrumentStack = tileInstrument.getInstrumentStack();
        }
        
        InstrumentDataUtils.setMidiSource(instrumentStack, message.midiSource, message.midiSourceName);
        InstrumentDataUtils.setEnabledChannelsString(instrumentStack, message.enabledChannelsString);
        InstrumentDataUtils.setSysInput(instrumentStack, message.sysInput);
        InstrumentDataUtils.setInstrumentVolume(instrumentStack, message.volume);
        
        if(tileInstrument == null) {
            sender.setItemInHand(message.handIn, instrumentStack);
        } else {
            tileInstrument.setInstrumentStack(instrumentStack);
            sender.level().sendBlockUpdated(tileInstrument.getBlockPos(), tileInstrument.getBlockState(), tileInstrument.getBlockState(), 2);
        }
    }
}
