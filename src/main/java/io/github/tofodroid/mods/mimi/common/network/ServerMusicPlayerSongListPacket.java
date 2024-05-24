package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ServerMusicPlayerSongListPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, ServerMusicPlayerSongListPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<ServerMusicPlayerSongListPacket> TYPE = new Type<>(ID);
    public static final Integer MAX_FILE_NAME_LENGTH = 200;

    public final UUID musicPlayerId;
    public final List<BasicMidiInfo> infos;
    public final List<Integer> favoriteIndicies;

    public ServerMusicPlayerSongListPacket(UUID musicPlayerId) {
        this.musicPlayerId = musicPlayerId;
        this.infos = new ArrayList<>();
        this.favoriteIndicies = new ArrayList<>();
    }

    public ServerMusicPlayerSongListPacket(UUID musicPlayerId, List<BasicMidiInfo> infos, List<Integer> favoriteIndicies) {
        this.musicPlayerId = musicPlayerId;
        this.favoriteIndicies = favoriteIndicies;

        if(infos.size() > 1000) {
            MIMIMod.LOGGER.warn("ServerMusicPlayerSongListPacket can only accept up to 1000 files. Trimming list ot 1000.");
            this.infos = new ArrayList<BasicMidiInfo>(infos).subList(0, 1000);
        } else {
            this.infos = infos;
        }
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
    }

    public static ServerMusicPlayerSongListPacket decodePacket(FriendlyByteBuf buf) {
        try {
            UUID musicPlayerId = buf.readUUID();
            byte numFavorites = buf.readByte();
            byte numInfos = buf.readByte();
            List<Integer> decodeFavorites = new ArrayList<>();
            List<BasicMidiInfo> decodeInfos = new ArrayList<>();

            for(int i = 0; i < numFavorites; i++) {
                decodeFavorites.add(Integer.valueOf(buf.readByte()));
            }

            for(int i = 0; i < numInfos; i++) {
                decodeInfos.add(new BasicMidiInfo(buf.readUtf(MAX_FILE_NAME_LENGTH), buf.readUUID(), buf.readBoolean()));
            }
            return new ServerMusicPlayerSongListPacket(musicPlayerId, decodeInfos, decodeFavorites);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMusicPlayerSongListPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMusicPlayerSongListPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.musicPlayerId);
        buf.writeByte(pkt.favoriteIndicies.size());
        buf.writeByte(pkt.infos.size());

        for(int i = 0; i < pkt.favoriteIndicies.size(); i++) {
            buf.writeByte(pkt.favoriteIndicies.get(i).byteValue());
        }

        for(int i = 0; i < pkt.infos.size(); i++) {
            buf.writeUtf(pkt.infos.get(i).fileName, MAX_FILE_NAME_LENGTH);
            buf.writeUUID(pkt.infos.get(i).fileId);
            buf.writeBoolean(pkt.infos.get(i).serverMidi);
        }
    }
}
