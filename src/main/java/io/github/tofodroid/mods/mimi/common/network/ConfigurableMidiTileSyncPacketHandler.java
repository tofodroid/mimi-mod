package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiTile;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ConfigurableMidiTileSyncPacketHandler {
    public static void handlePacket(final ConfigurableMidiTileSyncPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client received unexpected ConfigurableMidiTileSyncPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final ConfigurableMidiTileSyncPacket message, ServerPlayer sender) {
        AConfigurableMidiTile tile = (AConfigurableMidiTile)sender.level().getBlockEntity(message.tilePos);

        if(tile != null) {
            ItemStack midiStack = tile.getSourceStack();
            InstrumentDataUtils.setMidiSource(midiStack, message.midiSource, message.midiSourceName);
            InstrumentDataUtils.setEnabledChannelsString(midiStack, message.enabledChannelsString);
            InstrumentDataUtils.setFilterInstrument(midiStack, message.instrumentId);
            InstrumentDataUtils.setFilterNote(midiStack, message.filterNote);
            InstrumentDataUtils.setFilterOct(midiStack, message.filterOct);
            InstrumentDataUtils.setInvertInstrument(midiStack, message.invertInstrument);
            InstrumentDataUtils.setInvertNoteOct(midiStack, message.invertNoteOct);
            InstrumentDataUtils.setPublicBroadcast(midiStack, message.publicBroadcast);
            tile.setSourceStack(midiStack);
            sender.level().sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), 2);
        }
    }
}
