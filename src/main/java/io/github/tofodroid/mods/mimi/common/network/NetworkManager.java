package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String NET_PROTOCOL = "1";

    public static final SimpleChannel NET_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MIMIMod.MODID, "net_channel"))
            .networkProtocolVersion(() -> NET_PROTOCOL)
            .clientAcceptedVersions(NET_PROTOCOL::equals)
            .serverAcceptedVersions(NET_PROTOCOL::equals)
            .simpleChannel();
    
    public static void init(final FMLCommonSetupEvent event) {
        NET_CHANNEL.registerMessage(0, MidiNoteOnPacket.class, MidiNoteOnPacket::encodePacket, MidiNoteOnPacket::decodePacket, MidiNotePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(1, MidiNoteOffPacket.class, MidiNoteOffPacket::encodePacket, MidiNoteOffPacket::decodePacket, MidiNotePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(2, SpeakerNoteOnPacket.class, SpeakerNoteOnPacket::encodePacket, SpeakerNoteOnPacket::decodePacket, SpeakerNotePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(3, SpeakerNoteOffPacket.class, SpeakerNoteOffPacket::encodePacket, SpeakerNoteOffPacket::decodePacket, SpeakerNotePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(4, InstrumentItemDataUpdatePacket.class, InstrumentItemDataUpdatePacket::encodePacket, InstrumentItemDataUpdatePacket::decodePacket, InstrumentItemDataUpdatePacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(5, InstrumentTileDataUpdatePacket.class, InstrumentTileDataUpdatePacket::encodePacket, InstrumentTileDataUpdatePacket::decodePacket, InstrumentTileDataUpdatePacketHandler::handlePacket);
    }
}
