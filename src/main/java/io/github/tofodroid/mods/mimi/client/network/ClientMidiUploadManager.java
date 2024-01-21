package io.github.tofodroid.mods.mimi.client.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.IMidiFileProvider.LocalMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;

public abstract class ClientMidiUploadManager {
    // 6MB
    public static final Integer MAX_TOTAL_SEQUENCE_SIZE = 6000000;
        
    public static List<ServerMidiUploadPacket> generateUploadPackets(LocalMidiInfo file) {
        if(file.file.exists()) {
            try {
                byte[] bytes = MidiUtils.sequenceToByteArray(file.loadSequenceFromFile());
                List<byte[]> dataParts = new ArrayList<>();

                // Size cap
                if(bytes.length > MAX_TOTAL_SEQUENCE_SIZE) {
                    throw new IOException("Sequence is too large to upload. Size: " + bytes.length + " / " + MAX_TOTAL_SEQUENCE_SIZE);
                }

                // Split if necessary
                if(bytes.length > ServerMidiUploadPacket.MAX_DATA_SIZE) {
                    for(var i = 0; i < bytes.length; i += ServerMidiUploadPacket.MAX_DATA_SIZE) {
                        Integer rangeEnd = Math.min(i + ServerMidiUploadPacket.MAX_DATA_SIZE, bytes.length);
                        dataParts.add(Arrays.copyOfRange(bytes, i, rangeEnd));
                    }
                } else {
                    dataParts.add(bytes);
                }

                // Generate Packets
                List<ServerMidiUploadPacket> packets = new ArrayList<>();
                for(Integer i = 0; i < dataParts.size(); i++) {
                    packets.add(new ServerMidiUploadPacket(file.fileId, Integer.valueOf(dataParts.size()).byteValue(), i.byteValue(), dataParts.get(i)));
                };

                return packets;
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to upload MIDI Sequence: ", e);
            }
        }        

        return Arrays.asList(new ServerMidiUploadPacket(file.fileId).markFailed());
    }

    public static void uploadFileToServer(LocalMidiInfo file) {
        // Generate and send all packets
        for(ServerMidiUploadPacket packet : generateUploadPackets(file)) {
            sendServerBoundPacket(packet);
        }
    }

    public static void uploadFilePartsToServer(LocalMidiInfo file, byte[] parts) {
        // Generate and send subset of packets
        List<ServerMidiUploadPacket> packets = generateUploadPackets(file);
        for(byte part : parts) {
            sendServerBoundPacket(packets.get(part));
        }
    }

    public static void sendServerBoundPacket(final ServerMidiUploadPacket packet) {
        NetworkProxy.sendToServer(packet);
    }
    
    public static void handlePacket(final ServerMidiUploadPacket message) {
        if(message.fileId != null) {
            LocalMidiInfo fileInfo = MIMIMod.getProxy().clientMidiFiles().getInfoById(message.fileId);

            if(fileInfo != null) {
                if(message.data.length > 0) {
                    uploadFilePartsToServer(fileInfo, message.data);
                } else {
                    uploadFileToServer(fileInfo);
                }
            } else {
                NetworkProxy.sendToServer(new ServerMidiUploadPacket(message.fileId, Integer.valueOf(0).byteValue(), Integer.valueOf(0).byteValue(), new byte[]{}));
            }
        }
    }

}
