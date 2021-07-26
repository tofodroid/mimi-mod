package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;

public class ReceiverTileDataUpdatePacketHandler {
    public static void handlePacket(final ReceiverTileDataUpdatePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected ReceiverTileDataUpdatePacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final ReceiverTileDataUpdatePacket message, ServerPlayerEntity sender) {
        TileEntity tileEntity = sender.getServerWorld().getTileEntity(message.tilePos);
        TileReceiver receiverTile = tileEntity instanceof TileReceiver ? (TileReceiver) tileEntity : null;

        if (receiverTile != null) {
            receiverTile.setAcceptedChannelsString(message.acceptedChannelString);
            receiverTile.setMidiSource(message.midiSource);
            receiverTile.setFilterNoteString(message.filterNoteString);
            receiverTile.markDirty();
            sender.getServerWorld().getChunkProvider().markBlockChanged(receiverTile.getPos());
        }
    }
}
