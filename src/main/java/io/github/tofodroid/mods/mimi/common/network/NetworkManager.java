package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String NET_PROTOCOL = "1";

    public static final SimpleChannel NET_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MIMIMod.MODID, "net_channel"))
            .networkProtocolVersion(() -> NET_PROTOCOL)
            .clientAcceptedVersions(NET_PROTOCOL::equals)
            .serverAcceptedVersions(NET_PROTOCOL::equals)
            .simpleChannel();
    
    public static void init(final FMLCommonSetupEvent event) {
        NET_CHANNEL.registerMessage(0, MidiNotePacket.class, MidiNotePacket::encodePacket, MidiNotePacket::decodePacket, MidiNotePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(1, TransmitterNotePacket.class, TransmitterNotePacket::encodePacket, TransmitterNotePacket::decodePacket, TransmitterNotePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(2, SwitchboardStackUpdatePacket.class, SwitchboardStackUpdatePacket::encodePacket, SwitchboardStackUpdatePacket::decodePacket, SwitchboardStackUpdatePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(3, SyncItemInstrumentSwitchboardPacket.class, SyncItemInstrumentSwitchboardPacket::encodePacket, SyncItemInstrumentSwitchboardPacket::decodePacket, SyncItemInstrumentSwitchboardPacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(4, KeybindOpenInstrumentPacket.class, KeybindOpenInstrumentPacket::encodePacket, KeybindOpenInstrumentPacket::decodePacket, KeybindOpenInstrumentPacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(5, WriteDiskPacket.class, WriteDiskPacket::encodePacket, WriteDiskPacket::decodePacket, WriteDiskPacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(6, ServerMidiInfoPacket.class, ServerMidiInfoPacket::encodePacket, ServerMidiInfoPacket::decodePacket, ServerMidiInfoPacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(7, BroadcasterControlPacket.class, BroadcasterControlPacket::encodePacket, BroadcasterControlPacket::decodePacket, BroadcasterControlPacketHandler::handlePacket);
    }
}
