package io.github.tofodroid.mods.mimi.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacketPayload {
    public ResourceLocation id();
    public void write(FriendlyByteBuf buf);
}
