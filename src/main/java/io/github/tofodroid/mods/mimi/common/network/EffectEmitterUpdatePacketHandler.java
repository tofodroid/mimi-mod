package io.github.tofodroid.mods.mimi.common.network;

import java.util.Optional;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileEffectEmitter;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EffectEmitterUpdatePacketHandler {
    public static void handlePacketClient(final EffectEmitterUpdatePacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected EffectEmitterUpdatePacket!");
    }
    
    public static void handlePacketServer(final EffectEmitterUpdatePacket message, ServerPlayer sender) {
        // Apply
        Optional<TileEffectEmitter> tile = sender.getLevel().getBlockEntity(message.tilePos, ModTiles.EFFECTEMITTER);

        if(tile.isPresent()) {
            ItemStack sourceStack = tile.get().getSourceStack().copy();
            TagUtils.setOrRemoveBoolean(sourceStack, TileEffectEmitter.INVERTED_TAG, message.invertSignal);
            TagUtils.setOrRemoveString(sourceStack, TileEffectEmitter.SOUND_ID_TAG, message.sound);
            TagUtils.setOrRemoveString(sourceStack, TileEffectEmitter.PARTICLE_ID_TAG, message.particle);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.VOLUME_TAG, message.volume);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.PITCH_TAG, message.pitch);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.SIDE_TAG, message.side);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.SPREAD_TAG, message.spread);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.COUNT_TAG, message.count);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.SPEED_X_TAG, message.speed_x);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.SPEED_Y_TAG, message.speed_y);
            TagUtils.setOrRemoveByte(sourceStack, TileEffectEmitter.SPEED_Z_TAG, message.speed_z);
            TagUtils.setOrRemoveInt(sourceStack, TileEffectEmitter.SOUND_LOOP_TAG, message.sound_loop);
            TagUtils.setOrRemoveInt(sourceStack, TileEffectEmitter.PARTICLE_LOOP_TAG, message.particle_loop);
            tile.get().setSourceStack(sourceStack);
            sender.getLevel().sendBlockUpdated(tile.get().getBlockPos(), tile.get().getBlockState(), tile.get().getBlockState(), 2);
        }
    }
}
