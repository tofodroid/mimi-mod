package io.github.tofodroid.mods.mimi.neoforge.common;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkManager {
    public static <T extends CustomPacketPayload> void sendToServer(T message) {
        PacketDistributor.sendToServer(message);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static <T extends CustomPacketPayload> void sendToPlayersInRange(T message, BlockPos sourcePos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        PacketDistributor.sendToPlayersNear(worldIn, excludePlayer, sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(), range,  message);
    }

    public static void registerPackets(final PayloadRegistrar registrar) {
        registerMessage(registrar, MidiNotePacket.TYPE, MidiNotePacket::encodePacket, MidiNotePacket::decodePacket, MidiNotePacketHandler::handlePacketClient, MidiNotePacketHandler::handlePacketServer);
        registerMessage(registrar, SyncInstrumentPacket.TYPE, SyncInstrumentPacket::encodePacket, SyncInstrumentPacket::decodePacket, SyncInstrumentPacketHandler::handlePacketClient, SyncInstrumentPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerMusicPlayerStatusPacket.TYPE, ServerMusicPlayerStatusPacket::encodePacket, ServerMusicPlayerStatusPacket::decodePacket, ServerMusicPlayerStatusPacketHandler::handlePacketClient, ServerMusicPlayerStatusPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerMusicPlayerSongListPacket.TYPE, ServerMusicPlayerSongListPacket::encodePacket, ServerMusicPlayerSongListPacket::decodePacket, ServerMusicPlayerSongListPacketHandler::handlePacketClient, ServerMusicPlayerSongListPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerTimeSyncPacket.TYPE, ServerTimeSyncPacket::encodePacket, ServerTimeSyncPacket::decodePacket, ServerTimeSyncPacketHandler::handlePacketClient, ServerTimeSyncPacketHandler::handlePacketServer);
        registerMessage(registrar, ConfigurableMidiTileSyncPacket.TYPE, ConfigurableMidiTileSyncPacket::encodePacket, ConfigurableMidiTileSyncPacket::decodePacket, ConfigurableMidiTileSyncPacketHandler::handlePacketClient, ConfigurableMidiTileSyncPacketHandler::handlePacketServer);
        registerMessage(registrar, ClientMidiListPacket.TYPE, ClientMidiListPacket::encodePacket, ClientMidiListPacket::decodePacket, ClientMidiListPacketHandler::handlePacketClient, ClientMidiListPacketHandler::handlePacketServer);
        registerMessage(registrar, TransmitterControlPacket.TYPE, TransmitterControlPacket::encodePacket, TransmitterControlPacket::decodePacket, TransmitterControlPacketHandler::handlePacketClient, TransmitterControlPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerMidiUploadPacket.TYPE, ServerMidiUploadPacket::encodePacket, ServerMidiUploadPacket::decodePacket, ServerMidiUploadPacketHandler::handlePacketClient, ServerMidiUploadPacketHandler::handlePacketServer);
        registerMessage(registrar, EffectEmitterUpdatePacket.TYPE, EffectEmitterUpdatePacket::encodePacket, EffectEmitterUpdatePacket::decodePacket, EffectEmitterUpdatePacketHandler::handlePacketClient, EffectEmitterUpdatePacketHandler::handlePacketServer);
        registerMessage(registrar, MultiMidiNotePacket.TYPE, MultiMidiNotePacket::encodePacket, MultiMidiNotePacket::decodePacket, MultiMidiNotePacketHandler::handlePacketClient, MultiMidiNotePacketHandler::handlePacketServer);
        registerMessage(registrar, MidiDeviceBroadcastPacket.TYPE, MidiDeviceBroadcastPacket::encodePacket, MidiDeviceBroadcastPacket::decodePacket, MidiDeviceBroadcastPacketHandler::handlePacketClient, MidiDeviceBroadcastPacketHandler::handlePacketServer);
    }

    public static <T extends CustomPacketPayload> StreamDecoder<RegistryFriendlyByteBuf, T> createDecoder(Function<FriendlyByteBuf, T> decodeFunc) {
        return (buf) -> {
            return decodeFunc.apply(buf);
        };
    }

    public static <T extends CustomPacketPayload> StreamEncoder<RegistryFriendlyByteBuf, T> createEncoder(BiConsumer<T, FriendlyByteBuf> encodeFunc) {
        return (buf, pkt) -> {
            encodeFunc.accept(pkt, buf);
        };
    }

    public static <T extends CustomPacketPayload> void registerMessage(final PayloadRegistrar registrar, final Type<T> type, BiConsumer<T, FriendlyByteBuf> encodeFunc, Function<FriendlyByteBuf, T> decodeFunc, Consumer<T> handleClient, BiConsumer<T, ServerPlayer> handleServer) {
        registrar.playBidirectional(type, createCodec(encodeFunc, decodeFunc), createHandler(handleClient, handleServer));
    }

    public static <T extends CustomPacketPayload> StreamCodec<? super RegistryFriendlyByteBuf, T> createCodec(BiConsumer<T, FriendlyByteBuf> encodeFunc, Function<FriendlyByteBuf, T> decodeFunc) {
        return StreamCodec.of(createEncoder(encodeFunc), createDecoder(decodeFunc));
    }

    public static <T extends CustomPacketPayload> IPayloadHandler<T> createHandler(Consumer<T> handleClient, BiConsumer<T, ServerPlayer> handleServer) {
        return (final T message, final IPayloadContext ctx) -> {
            if(ctx.flow().isClientbound()) {
                ctx.enqueueWork(() -> handleClient.accept(message));
            } else {
                ctx.enqueueWork(() -> handleServer.accept(message, (ServerPlayer)ctx.player()));
            }
        };
    }
}
