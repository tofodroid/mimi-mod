package io.github.tofodroid.mods.mimi.forge.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ClientMidiListPacket;
import io.github.tofodroid.mods.mimi.common.network.ClientMidiListPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.ConfigurableMidiTileSyncPacket;
import io.github.tofodroid.mods.mimi.common.network.ConfigurableMidiTileSyncPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerSongListPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerSongListPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.ServerTimeSyncPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerTimeSyncPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.SyncInstrumentPacket;
import io.github.tofodroid.mods.mimi.common.network.SyncInstrumentPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkManager {
    private static final String NET_PROTOCOL = "2";

    private static final SimpleChannel MOD_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MIMIMod.MODID, "mod_channel"))
            .networkProtocolVersion(() -> NET_PROTOCOL)
            .clientAcceptedVersions(NET_PROTOCOL::equals)
            .serverAcceptedVersions(NET_PROTOCOL::equals)
            .simpleChannel();

    public static void sendToServer(Object message) {
        MOD_CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        MOD_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToPlayersInRange(Object message, BlockPos sourcePos, Float range) {

    }

    @SubscribeEvent
    public static void init(final FMLCommonSetupEvent event) {
        MOD_CHANNEL.registerMessage(0, MidiNotePacket.class, MidiNotePacket::encodePacket, MidiNotePacket::decodePacket, MidiNotePacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(1, SyncInstrumentPacket.class, SyncInstrumentPacket::encodePacket, SyncInstrumentPacket::decodePacket, SyncInstrumentPacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(2, ClientMidiListPacket.class, ClientMidiListPacket::encodePacket, ClientMidiListPacket::decodePacket, ClientMidiListPacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(3, ServerMusicPlayerStatusPacket.class, ServerMusicPlayerStatusPacket::encodePacket, ServerMusicPlayerStatusPacket::decodePacket, ServerMusicPlayerStatusPacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(4, ServerMusicPlayerSongListPacket.class, ServerMusicPlayerSongListPacket::encodePacket, ServerMusicPlayerSongListPacket::decodePacket, ServerMusicPlayerSongListPacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(5, ServerTimeSyncPacket.class, ServerTimeSyncPacket::encodePacket, ServerTimeSyncPacket::decodePacket, ServerTimeSyncPacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(6, ConfigurableMidiTileSyncPacket.class, ConfigurableMidiTileSyncPacket::encodePacket, ConfigurableMidiTileSyncPacket::decodePacket, ConfigurableMidiTileSyncPacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(7, TransmitterControlPacket.class, TransmitterControlPacket::encodePacket, TransmitterControlPacket::decodePacket, TransmitterControlPacketHandler::handlePacket);
        MOD_CHANNEL.registerMessage(8, ServerMidiUploadPacket.class, ServerMidiUploadPacket::encodePacket, ServerMidiUploadPacket::decodePacket, ServerMidiUploadPacketHandler::handlePacket);
    }
}
