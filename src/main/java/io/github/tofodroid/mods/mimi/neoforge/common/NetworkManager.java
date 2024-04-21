package io.github.tofodroid.mods.mimi.neoforge.common;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor.TargetPoint;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class NetworkManager {
    public static <T extends CustomPacketPayload> void sendToServer(T message) {
        PacketDistributor.SERVER.noArg().send(message);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T message, ServerPlayer player) {
        PacketDistributor.PLAYER.with(player).send(message);
    }

    public static <T extends CustomPacketPayload> void sendToPlayersInRange(T message, BlockPos sourcePos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        getPlayersInRangeTarget(sourcePos, worldIn, excludePlayer, range).send(message);
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

    public static void registerPackets(final IPayloadRegistrar registrar) {
        registerMessage(registrar, MidiNotePacket.ID, MidiNotePacket::decodePacket, MidiNotePacketHandler::handlePacketClient, MidiNotePacketHandler::handlePacketServer);
        registerMessage(registrar, SyncInstrumentPacket.ID, SyncInstrumentPacket::decodePacket, SyncInstrumentPacketHandler::handlePacketClient, SyncInstrumentPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerMusicPlayerStatusPacket.ID, ServerMusicPlayerStatusPacket::decodePacket, ServerMusicPlayerStatusPacketHandler::handlePacketClient, ServerMusicPlayerStatusPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerMusicPlayerSongListPacket.ID, ServerMusicPlayerSongListPacket::decodePacket, ServerMusicPlayerSongListPacketHandler::handlePacketClient, ServerMusicPlayerSongListPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerTimeSyncPacket.ID, ServerTimeSyncPacket::decodePacket, ServerTimeSyncPacketHandler::handlePacketClient, ServerTimeSyncPacketHandler::handlePacketServer);
        registerMessage(registrar, ConfigurableMidiTileSyncPacket.ID, ConfigurableMidiTileSyncPacket::decodePacket, ConfigurableMidiTileSyncPacketHandler::handlePacketClient, ConfigurableMidiTileSyncPacketHandler::handlePacketServer);
        registerMessage(registrar, ClientMidiListPacket.ID, ClientMidiListPacket::decodePacket, ClientMidiListPacketHandler::handlePacketClient, ClientMidiListPacketHandler::handlePacketServer);
        registerMessage(registrar, TransmitterControlPacket.ID, TransmitterControlPacket::decodePacket, TransmitterControlPacketHandler::handlePacketClient, TransmitterControlPacketHandler::handlePacketServer);
        registerMessage(registrar, ServerMidiUploadPacket.ID, ServerMidiUploadPacket::decodePacket, ServerMidiUploadPacketHandler::handlePacketClient, ServerMidiUploadPacketHandler::handlePacketServer);
        registerMessage(registrar, EffectEmitterUpdatePacket.ID, EffectEmitterUpdatePacket::decodePacket, EffectEmitterUpdatePacketHandler::handlePacketClient, EffectEmitterUpdatePacketHandler::handlePacketServer);
        registerMessage(registrar, MultiMidiNotePacket.ID, MultiMidiNotePacket::decodePacket, MultiMidiNotePacketHandler::handlePacketClient, MultiMidiNotePacketHandler::handlePacketServer);
        registerMessage(registrar, MidiDeviceBroadcastPacket.ID, MidiDeviceBroadcastPacket::decodePacket, MidiDeviceBroadcastPacketHandler::handlePacketClient, MidiDeviceBroadcastPacketHandler::handlePacketServer);
    }

    public static <T extends CustomPacketPayload> void registerMessage(final IPayloadRegistrar registrar, final ResourceLocation packetId, FriendlyByteBuf.Reader<T> decoder, Consumer<T> handleClient, BiConsumer<T, ServerPlayer> handleServer) {
        registrar.play(packetId, decoder, handler -> handler
            .client(createClientHandler(handleClient))
            .server(createServerHandler(handleServer))
        );
    }

    public static <T extends CustomPacketPayload> IPlayPayloadHandler<T> createClientHandler(Consumer<T> handleClient) {
        return (final T message, final PlayPayloadContext ctx) -> {
            ctx.workHandler().execute(() -> handleClient.accept(message));
        };
    }

    public static <T extends CustomPacketPayload> IPlayPayloadHandler<T> createServerHandler(BiConsumer<T, ServerPlayer> handleServer) {
        return (final T message, final PlayPayloadContext ctx) -> {
            ctx.workHandler().execute(() -> handleServer.accept(message, (ServerPlayer)ctx.player().get()));
        };
    }
}
