package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class KeybindOpenInstrumentPacketHandler {
    public static void handlePacket(final KeybindOpenInstrumentPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected KeybindOpenInstrumentPacketHandler!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final KeybindOpenInstrumentPacket message, ServerPlayer sender) {
        if(message.handheld) {
            Byte instrumentId = ItemInstrument.getEntityHeldInstrumentId(sender, message.handIn);

            if(instrumentId != null) {
                ItemInstrument heldInstrument = (ItemInstrument) ItemInstrument.getEntityHeldInstrumentStack(sender, message.handIn).getItem();
                NetworkHooks.openScreen(sender, heldInstrument.generateContainerProvider(message.handIn), buffer -> {
                    buffer.writeByte(instrumentId);
                    buffer.writeBoolean(true);
                    buffer.writeBoolean(InteractionHand.MAIN_HAND.equals(message.handIn));
                });
            }
        } else {
            TileInstrument tile = BlockInstrument.getTileInstrumentForEntity(sender);

            if(tile != null) {
                NetworkHooks.openScreen(sender, tile, buffer -> {
                    buffer.writeByte(tile.getInstrumentId());
                    buffer.writeBoolean(false);
                    buffer.writeBlockPos(tile.getBlockPos());
                });
            }
        }
    }
}
