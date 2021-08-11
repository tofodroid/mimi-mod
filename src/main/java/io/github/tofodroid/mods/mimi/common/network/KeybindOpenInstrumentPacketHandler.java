package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraft.util.Hand;

public class KeybindOpenInstrumentPacketHandler {
    public static void handlePacket(final KeybindOpenInstrumentPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected KeybindOpenInstrumentPacketHandler!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final KeybindOpenInstrumentPacket message, ServerPlayerEntity sender) {
        if(message.handheld) {
            Byte instrumentId = ItemInstrument.getEntityHeldInstrumentId(sender, message.handIn);

            if(instrumentId != null) {
                ItemInstrument heldInstrument = (ItemInstrument) ItemInstrument.getEntityHeldInstrumentStack(sender, message.handIn).getItem();
                NetworkHooks.openGui(sender, heldInstrument.generateContainerProvider(message.handIn), buffer -> {
                    buffer.writeByte(instrumentId);
                    buffer.writeBoolean(true);
                    buffer.writeBoolean(Hand.MAIN_HAND.equals(message.handIn));
                });
            }
        } else {
            TileInstrument tile = BlockInstrument.getTileInstrumentForEntity(sender);

            if(tile != null) {
                NetworkHooks.openGui(sender, tile, buffer -> {
                    buffer.writeByte(tile.getInstrumentId());
                    buffer.writeBoolean(false);
                    buffer.writeBlockPos(tile.getPos());
                });
            }
        }
    }
}
