package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sound.midi.Sequence;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerMidiUploadPacketHandler {
    // 6MB
    public static final Integer MAX_TOTAL_SEQUENCE_SIZE = 6000000;
    private static Map<UUID, Map<Integer,ServerMidiUploadPacket>> uploadsInProgress = new HashMap<>();

    public static void handlePacket(final ServerMidiUploadPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }

    public static Boolean startUploadMidiSequence(UUID fileId, Sequence sequence) {
        if(!uploadsInProgress.containsKey(fileId) && sequence.getMicrosecondLength() > 0) {
            MIMIMod.LOGGER.info("Starting upload for fileId: " + fileId.toString());
            try {
                byte[] bytes = MidiUtils.sequenceToByteArray(sequence);
                List<byte[]> dataParts = new ArrayList<>();
                Map<Integer, ServerMidiUploadPacket> packets = new HashMap<>();

                // Size cap
                if(bytes.length > MAX_TOTAL_SEQUENCE_SIZE) {
                    MIMIMod.LOGGER.warn("Sequence is too large to upload: " + fileId + ". Size: " + bytes.length);
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
                    packets.put(i, new ServerMidiUploadPacket(fileId, Integer.valueOf(dataParts.size()).byteValue(), i.byteValue(), dataParts.get(i)));
                };

                // Send Packets
                for(ServerMidiUploadPacket packet : packets.values()) {
                    sendServerBoundPacket(packet);
                }

                return true;
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to upload MIDI Sequence: ", e);
            }
        }

        return false;
    }

    public static void sendServerBoundPacket(final ServerMidiUploadPacket packet) {
        NetworkManager.SEQUENCE_CHANNEL.sendToServer(packet);
    }
    
    public static void handlePacketServer(final ServerMidiUploadPacket message, ServerPlayer sender) {
        Map<Integer, ServerMidiUploadPacket> filePackets = uploadsInProgress.containsKey(message.fileId) ? 
            uploadsInProgress.get(message.fileId) : new HashMap<>();
        filePackets.put(message.part.intValue(), message);

        // If all packets are accounted for, load
        if(filePackets.size() == message.totalParts && IntStream.range(0, filePackets.size()).allMatch(i -> filePackets.containsKey(i))) {
            MIMIMod.LOGGER.info("Received full upload for file: " + message.fileId);
            uploadsInProgress.remove(message.fileId);
            try {
                byte[] fullBytes = merge(filePackets.values().stream().map(p -> p.data).collect(Collectors.toList()));
                Sequence sequence = MidiUtils.byteArrayToSequence(fullBytes);

                // If player ID is upload ID then it's server playing local file
                if(sender.getUUID().equals(message.fileId)) {
                    ServerMusicPlayerMidiManager.createOrReplaceTransmitter(sender, sequence);
                } else {
                    // Save
                    MidiFileCacheManager.saveSequence(message.fileName, sequence, false);
                }
                NetworkManager.SEQUENCE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new ServerMidiUploadPacket(message.fileId, ServerMidiUploadPacket.UPLOAD_SUCCESS));
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to save MIDI data to file: ", e);
                NetworkManager.SEQUENCE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new ServerMidiUploadPacket(message.fileId, ServerMidiUploadPacket.UPLOAD_FAIL));
            }
        } else {
            MIMIMod.LOGGER.info("Received upload part " + (message.part+1) + "/" + message.totalParts + " for file: " + message.fileId);
            uploadsInProgress.put(message.fileId, filePackets);
        }
    }

    public static void handlePacketClient(final ServerMidiUploadPacket message) {
        if(message.totalParts == ServerMidiUploadPacket.UPLOAD_RESEND) {
            MIMIMod.LOGGER.info("Resend");
            sendServerBoundPacket(uploadsInProgress.get(message.fileId).get(message.part.intValue()));
        } else {
            uploadsInProgress.remove(message.fileId);
            ((ClientProxy)MIMIMod.proxy).getMidiInput().enderTransmitterManager.finishUploadSelectedLocalSongToServer(message);
        }
    }

    public static byte[] merge(List<byte[]> arrays) {
        int length = arrays.stream().collect(Collectors.summingInt(a -> a.length));
        int i = 0;
        byte[] result = new byte[length];

        for(byte[] a : arrays) {
            for(byte b : a){
                result[i] = b;
                i++;
            }
        }

        return result;
    }
}
