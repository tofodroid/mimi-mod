package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import net.minecraft.network.FriendlyByteBuf;

public class ClientMidiListPacket {
    public static final Integer MAX_FILE_NAME_LENGTH = 200;

    public final Boolean doUpdateServerFileList;
    public final List<BasicMidiInfo> infos;

    public ClientMidiListPacket() {
        this.doUpdateServerFileList = false;
        this.infos = new ArrayList<>();
    }

    public ClientMidiListPacket(Boolean doUpdateServerFileList, List<BasicMidiInfo> infos) {
        this.doUpdateServerFileList = doUpdateServerFileList;

        if(infos.size() > 50) {
            MIMIMod.LOGGER.warn("ClientMidiListPacket can only accept up to 50 files. Trimming list ot 50.");
            this.infos = new ArrayList<BasicMidiInfo>(infos).subList(0, 50);
        } else {
            this.infos = new ArrayList<BasicMidiInfo>(infos);
        }
    }

    public static ClientMidiListPacket decodePacket(FriendlyByteBuf buf) {
        try {
            Boolean doUpdateServerFileList = buf.readBoolean();
            byte numInfos = buf.readByte();
            List<BasicMidiInfo> decodeInfos = new ArrayList<>();

            for(int i = 0; i < numInfos; i++) {
                decodeInfos.add(new BasicMidiInfo(buf.readUtf(MAX_FILE_NAME_LENGTH), buf.readUUID(), false));
            }
            return new ClientMidiListPacket(doUpdateServerFileList, decodeInfos);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ClientMidiListPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ClientMidiListPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.doUpdateServerFileList);
        buf.writeByte(pkt.infos.size());
        for(int i = 0; i < pkt.infos.size(); i++) {
            buf.writeUtf(pkt.infos.get(i).fileName, MAX_FILE_NAME_LENGTH);
            buf.writeUUID(pkt.infos.get(i).fileId);
        }
    }
}
