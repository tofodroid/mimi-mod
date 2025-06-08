package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MIMIConfigPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceUtils.newModLocation(MIMIConfigPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<MIMIConfigPacket> TYPE = new Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
    }
    
    public static MIMIConfigPacket decodePacket(FriendlyByteBuf buf) {
        return new MIMIConfigPacket();
    }
    
    public static void encodePacket(MIMIConfigPacket pkt, FriendlyByteBuf buf) {
        // No-op
    }
}
