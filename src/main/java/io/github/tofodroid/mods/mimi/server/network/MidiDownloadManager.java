package io.github.tofodroid.mods.mimi.server.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sound.midi.Sequence;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacket;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MidiDownloadManager {
    private static final Integer REQUEST_MISSING_PARTS_EVERY_TICKS = 100;
    private static final Integer CANCEL_UPLOAD_AFTER_TICKS = 1000;
    private static Map<UUID, Map<Integer,ServerMidiUploadPacket>> DOWNLOADS_IN_PROGRESS = new HashMap<>();
    private static Map<UUID, Integer> TICKS_SINCE_LAST_PART = new HashMap<>();
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if(!TICKS_SINCE_LAST_PART.isEmpty()) {
            List<UUID> toRemove = new ArrayList<>();
            
            for(UUID playerId : TICKS_SINCE_LAST_PART.keySet()) {
                int count = TICKS_SINCE_LAST_PART.get(playerId);

                if(count > CANCEL_UPLOAD_AFTER_TICKS) {
                    toRemove.add(playerId);
                    ServerMidiUploadPacket packet = new ArrayList<>(DOWNLOADS_IN_PROGRESS.get(playerId).values()).get(0);
                    sendStatusToPlayer(packet.getSender(), packet.fileId,  ServerMidiUploadPacket.UPLOAD_FAIL);
                } else if(count > 0 && count % REQUEST_MISSING_PARTS_EVERY_TICKS == 0) {
                    ServerMidiUploadPacket packet = new ArrayList<>(DOWNLOADS_IN_PROGRESS.get(playerId).values()).get(0);
                    byte[] missingParts = getMissingParts(playerId, packet.totalParts);

                    if(missingParts.length == 0) {
                        toRemove.add(playerId);
                    } else {
                        requestParts(packet.getSender(), packet.fileId, missingParts);
                    }
                }

                TICKS_SINCE_LAST_PART.put(playerId, count+1);
            };

            for(UUID playerId : toRemove) {
                DOWNLOADS_IN_PROGRESS.remove(playerId);
                TICKS_SINCE_LAST_PART.remove(playerId);
            }
        }
    }

    public static Boolean isSameFileId(UUID playerId, UUID newFileId) {
        Map<Integer,ServerMidiUploadPacket> packets = DOWNLOADS_IN_PROGRESS.get(playerId);
        
        if(packets != null && packets.size() > 0) {
            return newFileId.equals(new ArrayList<>(DOWNLOADS_IN_PROGRESS.get(playerId).values()).get(0).fileId);
        }

        return false;
    }

    public static byte[] getMissingParts(UUID playerId, Byte expectedParts) {
        Map<Integer, ServerMidiUploadPacket> filePackets = DOWNLOADS_IN_PROGRESS.get(playerId);
        int[] parts = new int[]{};

        if(filePackets != null) {
            parts = IntStream.range(0, expectedParts).filter(i -> !filePackets.containsKey(i)).toArray();
        }

        byte[] result = new byte[parts.length];

        for(int i = 0; i < parts.length; i++) {
            result[i] = (byte) parts[i];
        }

        return result;
    }

    public static void handlePacket(final ServerMidiUploadPacket message, ServerPlayer sender) {
        if(!isSameFileId(sender.getUUID(), message.fileId) && !message.isRetry()) {
            DOWNLOADS_IN_PROGRESS.put(sender.getUUID(), new HashMap<>());
        }

        Map<Integer, ServerMidiUploadPacket> filePackets = DOWNLOADS_IN_PROGRESS.get(sender.getUUID());

        filePackets.put(message.part.intValue(), message.withSender(sender));

        // If all packets are accounted for, load
        if(partsAreAllPresent(message.fileId, filePackets, message.totalParts)) {
            MIMIMod.LOGGER.info("Received upload part " + (message.part+1) + "/" + message.totalParts + " for file: " + message.fileId);
            MIMIMod.LOGGER.info("Received full upload for file: " + message.fileId);
            TICKS_SINCE_LAST_PART.remove(sender.getUUID());
            Sequence sequence = loadSequenceFromParts(DOWNLOADS_IN_PROGRESS.remove(sender.getUUID()));

            if(sequence == null) {
                sendStatusToPlayer(sender, message.fileId,  ServerMidiUploadPacket.UPLOAD_FAIL);
                return;
            }

            if(message.fileName == null || message.fileName.isEmpty()) {
                ServerMusicPlayerMidiManager.createOrReplaceTransmitter(sender, sequence);
            } else {
                try {
                    MIMIMod.proxy.defaultMidiFiles().saveSequenceToCurrentFolder(message.fileName, sequence, false);
                } catch(Exception e) {
                    MIMIMod.LOGGER.error("Failed to save uploaded MIDI file. ", e);
                    sendStatusToPlayer(sender, message.fileId,  ServerMidiUploadPacket.UPLOAD_FAIL);
                }
            }
            sendStatusToPlayer(sender, message.fileId,  ServerMidiUploadPacket.UPLOAD_SUCCESS);
        } else {
            MIMIMod.LOGGER.info("Received upload part " + (message.part+1) + "/" + message.totalParts + " for file: " + message.fileId);
            DOWNLOADS_IN_PROGRESS.put(sender.getUUID(), filePackets);
            TICKS_SINCE_LAST_PART.put(sender.getUUID(), 1);
        }
    }

    public static Sequence loadSequenceFromParts(Map<Integer, ServerMidiUploadPacket> parts) {
        try {
            byte[] fullBytes = merge(parts.values().stream().map(p -> p.data).collect(Collectors.toList()));
            return MidiUtils.byteArrayToSequence(fullBytes);
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to load MIDI data from parts: ", e);
        }
        return null;
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

    public static Boolean partsAreAllPresent(UUID fileId, Map<Integer, ServerMidiUploadPacket> filePackets, Byte expectedParts) {
        return 
            filePackets.size() == expectedParts
            && filePackets.values().stream().allMatch(packet -> packet.fileId.equals(fileId))
            && IntStream.range(0, expectedParts).allMatch(i -> filePackets.containsKey(i))
        ;
    }

    public static void requestParts(ServerPlayer player, UUID fileId, byte[] parts) {
        MIMIMod.LOGGER.info("Requesting resend of parts " + Arrays.toString(parts) + " for file ID: " + fileId);
        NetworkManager.SEQUENCE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ServerMidiUploadPacket(fileId, parts));
    }

    public static void sendStatusToPlayer(ServerPlayer player, UUID fileId, Byte status) {
        NetworkManager.SEQUENCE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ServerMidiUploadPacket(fileId, status));
    }
}
