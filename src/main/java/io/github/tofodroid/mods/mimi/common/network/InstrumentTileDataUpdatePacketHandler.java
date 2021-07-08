package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

public class InstrumentTileDataUpdatePacketHandler {
    public static void handlePacket(final InstrumentTileDataUpdatePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected InstrumentTileUpdatePacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final InstrumentTileDataUpdatePacket message, ServerPlayerEntity sender) {
        TileEntity tileEntity = sender.getServerWorld().getTileEntity(message.tilePos);
        TileInstrument instrumentTile = tileEntity instanceof TileInstrument ? (TileInstrument) tileEntity : null;

        if (instrumentTile != null) {
            instrumentTile.setAcceptedChannelsString(message.acceptedChannelString);
            instrumentTile.setMaestro(message.maestroId);
            instrumentTile.markDirty();
            sender.getServerWorld().getChunkProvider().markBlockChanged(instrumentTile.getPos());
        }
    }
}
