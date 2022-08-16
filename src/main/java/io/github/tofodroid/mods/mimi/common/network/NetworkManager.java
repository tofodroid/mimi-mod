package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

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

    public static void init(final FMLCommonSetupEvent event) {
        NOTE_CHANNEL.registerMessage(0, MidiNotePacket.class, MidiNotePacket::encodePacket, MidiNotePacket::decodePacket, MidiNotePacketHandler::handlePacket);
        
        BROADCAST_CHANNEL.registerMessage(0, TransmitterNotePacket.class, TransmitterNotePacket::encodePacket, TransmitterNotePacket::decodePacket, TransmitterNotePacketHandler::handlePacket);
        
        INFO_CHANNEL.registerMessage(0, SwitchboardStackUpdatePacket.class, SwitchboardStackUpdatePacket::encodePacket, SwitchboardStackUpdatePacket::decodePacket, SwitchboardStackUpdatePacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(1, SyncItemInstrumentSwitchboardPacket.class, SyncItemInstrumentSwitchboardPacket::encodePacket, SyncItemInstrumentSwitchboardPacket::decodePacket, SyncItemInstrumentSwitchboardPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(2, KeybindOpenInstrumentPacket.class, KeybindOpenInstrumentPacket::encodePacket, KeybindOpenInstrumentPacket::decodePacket, KeybindOpenInstrumentPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(3, WriteDiskPacket.class, WriteDiskPacket::encodePacket, WriteDiskPacket::decodePacket, WriteDiskPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(4, ServerMusicPlayerStatusPacket.class, ServerMusicPlayerStatusPacket::encodePacket, ServerMusicPlayerStatusPacket::decodePacket, ServerMusicPlayerStatusPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(5, ServerMidiInfoPacket.class, ServerMidiInfoPacket::encodePacket, ServerMidiInfoPacket::decodePacket, ServerMidiInfoPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(6, BroadcastControlPacket.class, BroadcastControlPacket::encodePacket, BroadcastControlPacket::decodePacket, BroadcastControlPacketHandler::handlePacket);
        INFO_CHANNEL.registerMessage(7, ActiveTransmitterIdPacket.class, ActiveTransmitterIdPacket::encodePacket, ActiveTransmitterIdPacket::decodePacket, ActiveTransmitterIdPacketHandler::handlePacket);
    }
}
