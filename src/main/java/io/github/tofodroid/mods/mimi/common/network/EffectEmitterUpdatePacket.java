package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.TileEffectEmitter;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EffectEmitterUpdatePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, EffectEmitterUpdatePacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<EffectEmitterUpdatePacket> TYPE = new Type<>(ID);

    public final BlockPos tilePos;
    public final String sound;
    public final String particle;
    public final Byte volume;
    public final Byte pitch;
    public final Byte side;
    public final Byte spread;
    public final Byte count;
    public final Byte speed_x;
    public final Byte speed_y;
    public final Byte speed_z;
    public final Integer sound_loop;
    public final Integer particle_loop;
    public final Boolean invertSignal;

    public EffectEmitterUpdatePacket(ItemStack stack, BlockPos tilePos) {
        this(
            tilePos,
            TagUtils.getStringOrDefault(stack, TileEffectEmitter.SOUND_ID_TAG, ""),
            TagUtils.getStringOrDefault(stack, TileEffectEmitter.PARTICLE_ID_TAG, ""),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.VOLUME_TAG, 5),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.PITCH_TAG, 0),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.SIDE_TAG, 0),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.SPREAD_TAG, 0),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.COUNT_TAG, 1),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.SPEED_X_TAG, 0),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.SPEED_Y_TAG, 0),
            TagUtils.getByteOrDefault(stack, TileEffectEmitter.SPEED_Z_TAG, 0),
            TagUtils.getIntOrDefault(stack, TileEffectEmitter.SOUND_LOOP_TAG, 0),
            TagUtils.getIntOrDefault(stack, TileEffectEmitter.PARTICLE_LOOP_TAG, 0),
            TagUtils.getBooleanOrDefault(stack, TileEffectEmitter.INVERTED_TAG, false)
        );
    }

    public EffectEmitterUpdatePacket(BlockPos tilePos, String sound, String particle, Byte volume, Byte pitch, Byte side, Byte spread, Byte count, Byte speed_x, Byte speed_y, Byte speed_z, Integer sound_loop, Integer particle_loop, Boolean invertSignal) {
        this.tilePos = tilePos;
        this.sound = sound != null ? sound : "";
        this.particle = particle != null ? particle : "";
        this.volume = volume != null ? volume : 0;
        this.pitch = pitch != null ? pitch : 0;
        this.side = side != null ? side : 0;
        this.spread = spread != null ? spread : 0;
        this.count = count != null ? count : 0;
        this.speed_x = speed_x != null ? speed_x : 0;
        this.speed_y = speed_y != null ? speed_y : 0;
        this.speed_z = speed_z != null ? speed_z : 0;
        this.sound_loop = sound_loop != null ? sound_loop : 0;
        this.particle_loop = particle_loop != null ? particle_loop : 0;
        this.invertSignal = invertSignal != null ? invertSignal : false;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
    }

    public static EffectEmitterUpdatePacket decodePacket(FriendlyByteBuf buf) {
        try {
            BlockPos tilePos = buf.readBlockPos();
            String sound = buf.readUtf(512);
            String particle = buf.readUtf(512);
            Byte volume = buf.readByte();
            Byte pitch = buf.readByte();
            Byte side = buf.readByte();
            Byte spread = buf.readByte();
            Byte count = buf.readByte();
            Byte speed_x = buf.readByte();
            Byte speed_y = buf.readByte();
            Byte speed_z = buf.readByte();
            Integer sound_loop = buf.readInt();
            Integer particle_loop = buf.readInt();
            Boolean invertSignal = buf.readBoolean();

            return new EffectEmitterUpdatePacket(tilePos, sound, particle, volume, pitch, side, spread, count, speed_x, speed_y, speed_z, sound_loop, particle_loop, invertSignal);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("EffectEmitterUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("EffectEmitterUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(EffectEmitterUpdatePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.tilePos);
        buf.writeUtf(pkt.sound, 512);
        buf.writeUtf(pkt.particle, 512);
        buf.writeByte(pkt.volume);
        buf.writeByte(pkt.pitch);
        buf.writeByte(pkt.side);
        buf.writeByte(pkt.spread);
        buf.writeByte(pkt.count);
        buf.writeByte(pkt.speed_x);
        buf.writeByte(pkt.speed_y);
        buf.writeByte(pkt.speed_z);
        buf.writeInt(pkt.sound_loop);
        buf.writeInt(pkt.particle_loop);
        buf.writeBoolean(pkt.invertSignal);
    }
}
