package io.github.tofodroid.mods.mimi.server.events.broadcast.api;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.api.event.broadcast.IBroadcastConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class AServerBroadcastConsumer implements IBroadcastConsumer {
    public static final Byte ALL_CHANNELS_ID = Byte.MAX_VALUE;
    
    protected UUID ownerId;
    protected UUID linkedId;
    protected Supplier<BlockPos> blockPos;
    protected Supplier<ResourceKey<Level>> dimension;
    protected Integer enabledChannels;
    protected List<Byte> enabledChannelsList;

    public AServerBroadcastConsumer(UUID ownerId, UUID linkedId, Integer enabledChannels, List<Byte> enabledChannelsList, Supplier<BlockPos> pos, Supplier<ResourceKey<Level>> dimension) {
        this.ownerId = ownerId;
        this.linkedId = linkedId;
        this.blockPos = pos;
        this.dimension = dimension;
        this.enabledChannels = enabledChannels;
        this.enabledChannelsList = enabledChannelsList;
    }

    public AServerBroadcastConsumer(UUID ownerId, UUID linkedId, Integer enabledChannels, List<Byte> enabledChannelsList, BlockPos pos, ResourceKey<Level> dimension) {
        this(ownerId, linkedId, enabledChannels, enabledChannelsList, () -> pos, () -> dimension);
    }

    @Override
    public UUID getLinkedId() {
        return this.linkedId;
    }

    @Override
    public UUID getOwnerId() {
        return this.ownerId;
    }

    @Override
    public ResourceKey<Level> getConsumeDimension() {
        return this.dimension.get();
    }

    @Override
    public BlockPos getConsumePos() {
        return this.blockPos.get();
    }

    @Override
    public List<Byte> getEnabledChannelsList() {
        return this.enabledChannelsList;
    }

    public void tickConsumer() {/*Default no-op*/}
    public void close() throws Exception {this.onConsumerRemoved();}
}
