package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkManager {
    private static final String NET_PROTOCOL = "2";

    public static final SimpleChannel INFO_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MIMIMod.MODID, "info_channel"))
            .networkProtocolVersion(() -> NET_PROTOCOL)
            .clientAcceptedVersions(NET_PROTOCOL::equals)
            .serverAcceptedVersions(NET_PROTOCOL::equals)
            .simpleChannel();
    
    public static final SimpleChannel NOTE_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MIMIMod.MODID, "note_channel"))
            .networkProtocolVersion(() -> NET_PROTOCOL)
            .clientAcceptedVersions(NET_PROTOCOL::equals)
            .serverAcceptedVersions(NET_PROTOCOL::equals)
            .simpleChannel();
    
    public static final SimpleChannel BROADCAST_CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(MIMIMod.MODID, "broadcast_channel"))
        .networkProtocolVersion(() -> NET_PROTOCOL)
        .clientAcceptedVersions(NET_PROTOCOL::equals)
        .serverAcceptedVersions(NET_PROTOCOL::equals)
        .simpleChannel();

    public static final SimpleChannel SEQUENCE_CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(MIMIMod.MODID, "sequence_channel"))
        .networkProtocolVersion(() -> NET_PROTOCOL)
        .clientAcceptedVersions(NET_PROTOCOL::equals)
        .serverAcceptedVersions(NET_PROTOCOL::equals)
        .simpleChannel();

    @SubscribeEvent
    public static void init(final FMLCommonSetupEvent event) {
        NOTE_CHANNEL.registerMessage(0, MidiNotePacket.class, MidiNotePacket::encodePacket, MidiNotePacket::decodePacket, MidiNotePacketHandler::handlePacket);
        
        BROADCAST_CHANNEL.registerMessage(0, TransmitterNotePacket.class, TransmitterNotePacket::encodePacket, TransmitterNotePacket::decodePacket, TransmitterNotePacketHandler::handlePacket);
        
        INFO_CHANNEL.registerMessage(0, SyncInstrumentPacket.class, SyncInstrumentPacket::encodePacket, SyncInstrumentPacket::decodePacket, SyncInstrumentPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(1, ServerMidiListPacket.class, ServerMidiListPacket::encodePacket, ServerMidiListPacket::decodePacket, ServerMidiListPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(2, ServerMusicPlayerStatusPacket.class, ServerMusicPlayerStatusPacket::encodePacket, ServerMusicPlayerStatusPacket::decodePacket, ServerMusicPlayerStatusPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(4, ServerTimeSyncPacket.class, ServerTimeSyncPacket::encodePacket, ServerTimeSyncPacket::decodePacket, ServerTimeSyncPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(5, ConfigurableMidiTileSyncPacket.class, ConfigurableMidiTileSyncPacket::encodePacket, ConfigurableMidiTileSyncPacket::decodePacket, ConfigurableMidiTileSyncPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(6, TransmitterControlPacket.class, TransmitterControlPacket::encodePacket, TransmitterControlPacket::decodePacket, TransmitterControlPacketHandler::handlePacket);
    
        SEQUENCE_CHANNEL.registerMessage(0, ServerMidiUploadPacket.class, ServerMidiUploadPacket::encodePacket, ServerMidiUploadPacket::decodePacket, ServerMidiUploadPacketHandler::handlePacket);

    }
}
