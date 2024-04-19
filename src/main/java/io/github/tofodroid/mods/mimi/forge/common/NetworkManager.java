package io.github.tofodroid.mods.mimi.forge.common;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ClientMidiListPacket;
import io.github.tofodroid.mods.mimi.common.network.ClientMidiListPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.ConfigurableMidiTileSyncPacket;
import io.github.tofodroid.mods.mimi.common.network.ConfigurableMidiTileSyncPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.EffectEmitterUpdatePacket;
import io.github.tofodroid.mods.mimi.common.network.EffectEmitterUpdatePacketHandler;
import io.github.tofodroid.mods.mimi.common.network.MidiDeviceBroadcastPacket;
import io.github.tofodroid.mods.mimi.common.network.MidiDeviceBroadcastPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import io.github.tofodroid.mods.mimi.common.network.MultiMidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MultiMidiNotePacketHandler;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.PacketDistributor.TargetPoint;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkManager {
    private static final Integer NET_PROTOCOL = 2;

    private static final SimpleChannel MOD_CHANNEL = ChannelBuilder
            .named(new ResourceLocation(MIMIMod.MODID, "mod_channel"))
            .networkProtocolVersion(NET_PROTOCOL)
            .simpleChannel();

    public static void sendToServer(Object message) {
        MOD_CHANNEL.send(message, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        MOD_CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToPlayersInRange(Object message, BlockPos sourcePos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        MOD_CHANNEL.send(message, getPlayersInRangeTarget(sourcePos, worldIn, excludePlayer, range));
    }

    protected static PacketDistributor.PacketTarget getPlayersInRangeTarget(BlockPos targetPos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        TargetPoint target;

        if(excludePlayer == null) {
            target = new PacketDistributor.TargetPoint(targetPos.getX(), targetPos.getY(), targetPos.getZ(), range, worldIn.dimension());
        } else {
            target = new PacketDistributor.TargetPoint(excludePlayer, targetPos.getX(), targetPos.getY(), targetPos.getZ(), range, worldIn.dimension());
        }
        return PacketDistributor.NEAR.with(target);
    }

    @SubscribeEvent
    public static void init(final FMLCommonSetupEvent event) {
        registerMessage(MidiNotePacket.class, MidiNotePacket::encodePacket, MidiNotePacket::decodePacket, createHandler(MidiNotePacketHandler::handlePacketClient, MidiNotePacketHandler::handlePacketServer));
        registerMessage(SyncInstrumentPacket.class, SyncInstrumentPacket::encodePacket, SyncInstrumentPacket::decodePacket, createHandler(SyncInstrumentPacketHandler::handlePacketClient, SyncInstrumentPacketHandler::handlePacketServer));
        registerMessage(ClientMidiListPacket.class, ClientMidiListPacket::encodePacket, ClientMidiListPacket::decodePacket, createHandler(ClientMidiListPacketHandler::handlePacketClient, ClientMidiListPacketHandler::handlePacketServer));
        registerMessage(ServerMusicPlayerStatusPacket.class, ServerMusicPlayerStatusPacket::encodePacket, ServerMusicPlayerStatusPacket::decodePacket, createHandler(ServerMusicPlayerStatusPacketHandler::handlePacketClient, ServerMusicPlayerStatusPacketHandler::handlePacketServer));
        registerMessage(ServerMusicPlayerSongListPacket.class, ServerMusicPlayerSongListPacket::encodePacket, ServerMusicPlayerSongListPacket::decodePacket, createHandler(ServerMusicPlayerSongListPacketHandler::handlePacketClient, ServerMusicPlayerSongListPacketHandler::handlePacketServer));
        registerMessage(ServerTimeSyncPacket.class, ServerTimeSyncPacket::encodePacket, ServerTimeSyncPacket::decodePacket, createHandler(ServerTimeSyncPacketHandler::handlePacketClient, ServerTimeSyncPacketHandler::handlePacketServer));
        registerMessage(ConfigurableMidiTileSyncPacket.class, ConfigurableMidiTileSyncPacket::encodePacket, ConfigurableMidiTileSyncPacket::decodePacket, createHandler(ConfigurableMidiTileSyncPacketHandler::handlePacketClient, ConfigurableMidiTileSyncPacketHandler::handlePacketServer));
        registerMessage(TransmitterControlPacket.class, TransmitterControlPacket::encodePacket, TransmitterControlPacket::decodePacket, createHandler(TransmitterControlPacketHandler::handlePacketClient, TransmitterControlPacketHandler::handlePacketServer));
        registerMessage(ServerMidiUploadPacket.class, ServerMidiUploadPacket::encodePacket, ServerMidiUploadPacket::decodePacket, createHandler(ServerMidiUploadPacketHandler::handlePacketClient, ServerMidiUploadPacketHandler::handlePacketServer));
        registerMessage(EffectEmitterUpdatePacket.class, EffectEmitterUpdatePacket::encodePacket, EffectEmitterUpdatePacket::decodePacket, createHandler(EffectEmitterUpdatePacketHandler::handlePacketClient, EffectEmitterUpdatePacketHandler::handlePacketServer));
        registerMessage(MultiMidiNotePacket.class, MultiMidiNotePacket::encodePacket, MultiMidiNotePacket::decodePacket, createHandler(MultiMidiNotePacketHandler::handlePacketClient, MultiMidiNotePacketHandler::handlePacketServer));
        registerMessage(MidiDeviceBroadcastPacket.class, MidiDeviceBroadcastPacket::encodePacket, MidiDeviceBroadcastPacket::decodePacket, createHandler(MidiDeviceBroadcastPacketHandler::handlePacketClient, MidiDeviceBroadcastPacketHandler::handlePacketServer));
    }

    public static <T> void registerMessage(Class<T> messageClass, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, CustomPayloadEvent.Context> handler) {
        MOD_CHANNEL.messageBuilder(messageClass)
            .encoder(encoder)
            .decoder(decoder)
            .consumerNetworkThread(handler)
            .add();
    }

    public static <T> BiConsumer<T, CustomPayloadEvent.Context> createHandler(Consumer<T> handleClient, BiConsumer<T, ServerPlayer> handleServer) {
        return (T message, CustomPayloadEvent.Context ctx) -> {
            if(ctx.getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
                ctx.enqueueWork(() -> handleServer.accept(message, ctx.getSender()));
            } else {
                ctx.enqueueWork(() -> handleClient.accept(message));
            }
            ctx.setPacketHandled(true);
        };
    }
}
