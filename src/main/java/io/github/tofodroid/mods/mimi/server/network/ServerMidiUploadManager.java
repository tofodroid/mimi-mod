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
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacket;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerMidiUploadManager {
    private static final Integer REQUEST_MISSING_PARTS_EVERY_TICKS = 100;
    private static final Integer CANCEL_UPLOAD_AFTER_TICKS = 1000;
    private static Map<UUID, UUID> UPLOAD_CLIENTS = new HashMap<>();
    private static Map<UUID, BasicMidiInfo> UPLOAD_INFOS = new HashMap<>();
    private static Map<UUID, Map<Integer,ServerMidiUploadPacket>> UPLOAD_PARTS = new HashMap<>();
    private static Map<UUID, Integer> TICKS_SINCE_LAST_PART = new HashMap<>();
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if(!TICKS_SINCE_LAST_PART.isEmpty()) {
            List<UUID> toRemove = new ArrayList<>();
            
            for(UUID fileId : TICKS_SINCE_LAST_PART.keySet()) {
                int count = TICKS_SINCE_LAST_PART.get(fileId);
                ServerPlayer player = getServerPlayerById(UPLOAD_CLIENTS.get(fileId));

                if(player == null || count > CANCEL_UPLOAD_AFTER_TICKS) {
                    toRemove.add(fileId);
                    // TODO: Notify music player
                } else if(count > 0 && count % REQUEST_MISSING_PARTS_EVERY_TICKS == 0) {
                    if(UPLOAD_PARTS.get(fileId).values().isEmpty()) {
                        requestParts(player, fileId, new byte[]{1});
                    } else {
                        ServerMidiUploadPacket packet = new ArrayList<>(UPLOAD_PARTS.get(fileId).values()).get(0);
                        byte[] missingParts = getMissingParts(fileId, packet.totalParts);

                        if(missingParts.length == 0) {
                            toRemove.add(fileId);
                        } else {
                            requestParts(player, fileId, missingParts);
                        }
                    }
                }

                TICKS_SINCE_LAST_PART.put(fileId, count+1);
            };

            for(UUID fileId : toRemove) {
                UPLOAD_CLIENTS.remove(fileId);
                UPLOAD_PARTS.remove(fileId);
                UPLOAD_INFOS.remove(fileId);
                TICKS_SINCE_LAST_PART.remove(fileId);
            }
        }
    }

    public static void startUploadRequest(UUID clientId, BasicMidiInfo info) {
        ServerPlayer player = getServerPlayerById(clientId);

        if(!UPLOAD_PARTS.containsKey(info.fileId) && player != null) {
            UPLOAD_PARTS.put(info.fileId, new HashMap<>());
            UPLOAD_INFOS.put(info.fileId, info);
            UPLOAD_CLIENTS.put(info.fileId, clientId);
            TICKS_SINCE_LAST_PART.put(info.fileId, 0);
            NetworkManager.SEQUENCE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ServerMidiUploadPacket(info.fileId));
        }
    }

    public static Boolean isSameSender(UUID fileId, UUID clientId) {
        Map<Integer,ServerMidiUploadPacket> packets = UPLOAD_PARTS.get(fileId);
        
        if(packets != null && packets.size() > 0) {
            return clientId.equals(new ArrayList<>(UPLOAD_PARTS.get(fileId).values()).get(0).getSender().getUUID());
        }

        return false;
    }

    public static ServerPlayer getServerPlayerById(UUID clientId) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(clientId);
    }

    public static byte[] getMissingParts(UUID fileId, Byte expectedParts) {
        Map<Integer, ServerMidiUploadPacket> filePackets = UPLOAD_PARTS.get(fileId);
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
        if(!isSameSender(message.fileId, sender.getUUID())) {
            // Ignore - TODO?
        }

        Map<Integer, ServerMidiUploadPacket> filePackets = UPLOAD_PARTS.get(message.fileId);

        filePackets.put(message.part.intValue(), message.withSender(sender));

        // If all packets are accounted for, load
        if(partsAreAllPresent(message.fileId, filePackets, message.totalParts)) {
            MIMIMod.LOGGER.info("Received upload part " + (message.part+1) + "/" + message.totalParts + " for file: " + message.fileId);
            MIMIMod.LOGGER.info("Received full upload for file: " + message.fileId);
            TICKS_SINCE_LAST_PART.remove(message.fileId);
            Sequence sequence = loadSequenceFromParts(UPLOAD_PARTS.remove(message.fileId));
            BasicMidiInfo info = UPLOAD_INFOS.remove(message.fileId);
            UPLOAD_CLIENTS.remove(message.fileId);

            if(sequence == null) {
                // TODO - Handle fail
            }
            ServerMusicPlayerManager.onFinishLoadSequence(info, sequence);
        } else {
            MIMIMod.LOGGER.info("Received upload part " + (message.part+1) + "/" + message.totalParts + " for file: " + message.fileId);
            UPLOAD_PARTS.put(message.fileId, filePackets);
            TICKS_SINCE_LAST_PART.put(message.fileId, 0);
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
}
