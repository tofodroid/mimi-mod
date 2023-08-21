package io.github.tofodroid.mods.mimi.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ServerMidiListPacket {
    public final List<MidiFileInfo> midiList;

    public ServerMidiListPacket() {
        this(Arrays.asList());
    }

    public ServerMidiListPacket(List<MidiFileInfo> midiList) {
        this.midiList = midiList;
    }

    public static ServerMidiListPacket decodePacket(FriendlyByteBuf buf) {
        try {
            List<MidiFileInfo> midiList = deserialize(buf.readOptional(FriendlyByteBuf::readByteArray));
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

    public static List<MidiFileInfo> deserialize(Optional<byte[]> bytes) {
        if(bytes.isPresent()) {
            List<MidiFileInfo> list = new ArrayList<>();
            SerializedMidiFileInfo[] midiArray;            
            
            try(ByteArrayInputStream bis = new ByteArrayInputStream(bytes.get())){
                ObjectInputStream ois = new ObjectInputStream(bis);
                midiArray = (SerializedMidiFileInfo[])ois.readObject();
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to deserialize MIDIFileInfo: ", e);
                return Arrays.asList();
            }

            for(int i = 0; i < midiArray.length; i++) {
                SerializedMidiFileInfo info = midiArray[i]; 
                MidiFileInfo fullInfo = new MidiFileInfo();
                fullInfo.file = new File(info.fileName);
                fullInfo.byteChannelMapping = info.channelMapping;
                fullInfo.instrumentMapping = MidiFileInfo.getInstrumentMapping(info.channelMapping);
                fullInfo.songLength = info.songLengthSeconds;
                list.add(fullInfo);
            }
            return list;
        }
        return Arrays.asList();
    }

    public static Optional<byte[]> serialize(List<MidiFileInfo> midiList) {
        if(!midiList.isEmpty()) {
            SerializedMidiFileInfo[] midiArray = new SerializedMidiFileInfo[midiList.size()];
            for(int i = 0; i < midiList.size(); i++) {
                MidiFileInfo info = midiList.get(i);
                midiArray[i] = new SerializedMidiFileInfo(info.file.getName(), info.byteChannelMapping, info.songLength);
            }
            
            try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(midiArray);
                return Optional.of(bos.toByteArray());
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to serialize MIDIFileInfo: ", e);
            }
        }
        return Optional.empty();        
    }
    
}
