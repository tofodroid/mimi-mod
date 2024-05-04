package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ClientMidiListPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, ClientMidiListPacket.class.getSimpleName().toLowerCase());
    public static final Integer MAX_FILE_NAME_LENGTH = 200;
    public final List<BasicMidiInfo> infos;

    public ClientMidiListPacket() {
        this.infos = new ArrayList<>();
    }

    public ClientMidiListPacket(List<BasicMidiInfo> infos) {
        if(infos.size() > 50) {
            MIMIMod.LOGGER.warn("ClientMidiListPacket can only accept up to 50 files. Trimming list ot 50.");
            this.infos = new ArrayList<BasicMidiInfo>(infos).subList(0, 50);
        } else {
            this.infos = new ArrayList<BasicMidiInfo>(infos);
        }
    }

    @Override
    public ResourceLocation id() {
        return ClientMidiListPacket.ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        ClientMidiListPacket.encodePacket(this, buf);
    }

    public static ClientMidiListPacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte numInfos = buf.readByte();
            List<BasicMidiInfo> decodeInfos = new ArrayList<>();

            for(int i = 0; i < numInfos; i++) {
                decodeInfos.add(new BasicMidiInfo(buf.readUtf(MAX_FILE_NAME_LENGTH), buf.readUUID(), false));
            }
            return new ClientMidiListPacket(decodeInfos);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ClientMidiListPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ClientMidiListPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.infos.size());
        for(int i = 0; i < pkt.infos.size(); i++) {
            buf.writeUtf(pkt.infos.get(i).fileName, MAX_FILE_NAME_LENGTH);
            buf.writeUUID(pkt.infos.get(i).fileId);
        }
    }
}
