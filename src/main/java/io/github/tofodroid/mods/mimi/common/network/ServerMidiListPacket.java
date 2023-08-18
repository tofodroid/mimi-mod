package io.github.tofodroid.mods.mimi.common.network;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SerializationUtils;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ServerMidiListPacket {
    public final Optional<List<MidiFileInfo>> midiList;

    public ServerMidiListPacket() {
        this(Optional.empty());
    }

    public ServerMidiListPacket(List<MidiFileInfo> midiList) {
        if(!midiList.isEmpty()) {
            this.midiList = Optional.of(midiList);
        } else {
            this.midiList = Optional.empty();
        }
    }
    
    protected ServerMidiListPacket(Optional<List<MidiFileInfo>> midiList) {
        this.midiList = midiList;
    }

    public static ServerMidiListPacket decodePacket(FriendlyByteBuf buf) {
        try {
            Optional<List<MidiFileInfo>> midiList = deserialize(buf.readOptional(FriendlyByteBuf::readByteArray));
            return new ServerMidiListPacket(midiList);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMidiListPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerMidiListPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMidiListPacket pkt, FriendlyByteBuf buf) {
        buf.writeOptional(serialize(pkt.midiList), FriendlyByteBuf::writeByteArray);
    }

    public static Optional<List<MidiFileInfo>> deserialize(Optional<byte[]> bytes) {
        if(bytes.isPresent()) {
            List<MidiFileInfo> list = new ArrayList<>();
            SerializedMidiFileInfo[] midiArray = SerializationUtils.deserialize(bytes.get());
            for(int i = 0; i < midiArray.length; i++) {
                SerializedMidiFileInfo info = midiArray[i]; 
                MidiFileInfo fullInfo = new MidiFileInfo();
                fullInfo.file = new File(info.fileName);
                fullInfo.byteChannelMapping = info.channelMapping;
                fullInfo.songLength = info.songLengthSeconds;
                list.add(fullInfo);
            }
            return Optional.of(list);
        }
        return Optional.empty();
    }

    public static Optional<byte[]> serialize(Optional<List<MidiFileInfo>> midiList) {
        if(midiList.isPresent() && !midiList.get().isEmpty()) {
            SerializedMidiFileInfo[] midiArray = new SerializedMidiFileInfo[midiList.get().size()];
            for(int i = 0; i < midiList.get().size(); i++) {
                MidiFileInfo info = midiList.get().get(i);
                midiArray[i] = new SerializedMidiFileInfo(info.file.getName(), info.byteChannelMapping, info.songLength);
            }
            return Optional.of(SerializationUtils.serialize(midiArray));
        }
        return Optional.empty();        
    }
    
    public static class SerializedMidiFileInfo implements Serializable  {
        public final String fileName;        
        public final byte[] channelMapping;
        public final Integer songLengthSeconds;

        public SerializedMidiFileInfo(String fileName, byte[] channelMapping, Integer songLengthSeconds) {
            this.fileName = fileName;
            this.channelMapping = channelMapping;
            this.songLengthSeconds = songLengthSeconds;
        }
    }
}
