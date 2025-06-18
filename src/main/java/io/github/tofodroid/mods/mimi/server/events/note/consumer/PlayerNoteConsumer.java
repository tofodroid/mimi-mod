package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import java.util.ArrayList;
import java.util.Map;

import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.common.network.MultiNoteEventPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.server.events.note.api.ANoteConsumer;
import io.github.tofodroid.mods.mimi.util.EntityUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class PlayerNoteConsumer extends ANoteConsumer {
    private ServerPlayer player;
    private Map<Long, ArrayList<NoteEvent>> packetCacheMap = new Long2ObjectOpenHashMap<>();
    private BlockPos cachedPos;
    private ResourceKey<Level> cachedDimension;

    public PlayerNoteConsumer(ServerPlayer player) {
        super(player.getUUID(), ALL_INSTRUMENTS_ID);
        this.player = player;
        this.cachedPos = EntityUtils.getEntityHeadPos(this.player);
        this.cachedDimension = this.player.level().dimension();
    }

    protected Boolean wasSentBySelf(NoteEvent event) {
        return event.clientSource && event.senderId != null && this.player.getUUID().equals(event.senderId);
    }

    @Override
    public Boolean willHandleEvent(NoteEvent message) {
        return !wasSentBySelf(message);
    }

    @Override
    public void doHandleEvent(NoteEvent message) {
        cacheEvent(message);
    }

    protected Boolean playerIsAlive() {
        return this.player != null && !this.player.isRemoved() && !this.player.isDeadOrDying();
    }

    @Override
    public void tickConsumer() {
        if(!packetCacheMap.isEmpty() && this.playerIsAlive()) {
            NetworkProxy.sendToPlayer(new MultiNoteEventPacket(packetCacheMap), this.player);
            packetCacheMap.clear();
        }
        cachedPos = EntityUtils.getEntityHeadPos(this.player);
        cachedDimension = this.player.level().dimension();
    }

    @Override
    public void onConsumerRemoved() { /* No-op */ }

    private void cacheEvent(NoteEvent event) {
        if(event.senderId != null && event.pos != null && event.eventTime != null) {
            ArrayList<NoteEvent> eventList = packetCacheMap.getOrDefault(event.eventTime, new ArrayList<>());
            eventList.add(event);
            packetCacheMap.put(event.eventTime, eventList);
        }
    }

    @Override
    public BlockPos getBlockPos() {
        return this.cachedPos;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return this.cachedDimension;
    }
}
