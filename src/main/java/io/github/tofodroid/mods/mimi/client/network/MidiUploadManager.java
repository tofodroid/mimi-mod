package io.github.tofodroid.mods.mimi.client.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sound.midi.Sequence;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacket;

public abstract class MidiUploadManager {
    // 6MB
    public static final Integer MAX_TOTAL_SEQUENCE_SIZE = 6000000;

    private static Map<Integer,ServerMidiUploadPacket> UPLOAD_IN_PROGRESS_PARTS = new HashMap<>();
    private static UUID UPLOAD_IN_PROGRESS_FILE_ID;

    public static Boolean uploadInProgress() {
        return !UPLOAD_IN_PROGRESS_PARTS.isEmpty();
    }
        
    public static Boolean startUploadSequenceToServer(Sequence sequence) {
        UPLOAD_IN_PROGRESS_PARTS.clear();
        UPLOAD_IN_PROGRESS_FILE_ID = UUID.randomUUID();

        MIMIMod.LOGGER.info("*** Starting upload for fileId: " + UPLOAD_IN_PROGRESS_FILE_ID.toString());

        try {
            byte[] bytes = MidiUtils.sequenceToByteArray(sequence);
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

            // Generate and Cache Packets
            for(Integer i = 0; i < dataParts.size(); i++) {
                UPLOAD_IN_PROGRESS_PARTS.put(i, new ServerMidiUploadPacket(UPLOAD_IN_PROGRESS_FILE_ID, Integer.valueOf(dataParts.size()).byteValue(), i.byteValue(), dataParts.get(i)));
            };

            // Send Packets
            for(ServerMidiUploadPacket packet : UPLOAD_IN_PROGRESS_PARTS.values()) {
                sendServerBoundPacket(packet);
            }

            return true;
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to upload MIDI Sequence: ", e);
        }

        return false;
    }

    public static void sendServerBoundPacket(final ServerMidiUploadPacket packet) {
        NetworkManager.SEQUENCE_CHANNEL.sendToServer(packet);
    }
    
    public static void handlePacket(final ServerMidiUploadPacket message) {
        if(message.fileId != null && message.fileId.equals(UPLOAD_IN_PROGRESS_FILE_ID)) {
            if(message.getResponseStatus() == ServerMidiUploadPacket.UPLOAD_RESEND) {
                for(int i : message.data) {
                    MIMIMod.LOGGER.info("Resending part " + i + " for fileId: " + message.fileId);
                    sendServerBoundPacket(UPLOAD_IN_PROGRESS_PARTS.get(i).markRetry());
                }
            } else {
                ((ClientProxy)MIMIMod.proxy).getMidiInput().enderTransmitterManager.finishUploadSelectedLocalSongToServer(message);
                UPLOAD_IN_PROGRESS_PARTS.clear();
                UPLOAD_IN_PROGRESS_FILE_ID = null;
            }
        } else {
            MIMIMod.LOGGER.warn("Client received ServerMidiUploadPacket response for old file ID. Ignoring.");
        }        
    }

}
