package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import java.util.ArrayList;
import java.util.Map;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MultiMidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetMidiEvent;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.server.events.note.NoteEvent;
import io.github.tofodroid.mods.mimi.server.events.note.api.ANoteConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class PlayerNoteConsumer extends ANoteConsumer {
    private ServerPlayer player;
    private Map<Long, ArrayList<NetMidiEvent>> packetCacheMap = new Long2ObjectOpenHashMap<>();
    private BlockPos cachedPos;
    private ResourceKey<Level> cachedDimension;

    public PlayerNoteConsumer(ServerPlayer player) {
        super(player.getUUID(), ALL_INSTRUMENTS_ID);
        this.player = player;
        this.cachedPos = this.player.getOnPos();
        this.cachedDimension = this.player.level().dimension();
    }

    @Override
    protected Boolean willHandleNoteOn(NoteEvent message) {
        if(message.senderId != null) {
            return !this.player.getUUID().equals(message.senderId);
        }
        return true;
    }

    @Override
    protected Boolean doHandleNoteOn(NoteEvent message) {
        cachePacket(message.event);
        return true;
    }

    @Override
    protected Boolean willHandleNoteOff(NoteEvent message) {
        return message.senderId == null || !this.player.getUUID().equals(message.senderId);
    }

    @Override
    protected Boolean doHandleNoteOff(NoteEvent message) {
        cachePacket(message.event);
        return true;
    }

    @Override
    protected Boolean willHandleAllNotesOff(NoteEvent message) {
        return message.senderId == null || !this.player.getUUID().equals(message.senderId);
    }

    @Override
    protected Boolean doHandleAllNotesOff(NoteEvent message) {
        cachePacket(message.event);
        return true;
    }

    @Override
    protected Boolean willHandleControl(NoteEvent message) {
        return message.senderId == null || !this.player.getUUID().equals(message.senderId);
    }

    @Override
    protected Boolean doHandleControl(NoteEvent message) {
        cachePacket(message.event);
        return true;
    }

    protected Boolean playerIsAlive() {
        return this.player != null && !this.player.isRemoved() && !this.player.isDeadOrDying();
    }

    @Override
    public void tick() {
        if(!packetCacheMap.isEmpty() && this.playerIsAlive()) {
            NetworkProxy.sendToPlayer(new MultiMidiNotePacket(packetCacheMap), this.player);
            packetCacheMap.clear();
        }
        cachedPos = this.player.getOnPos();
        cachedDimension = this.player.level().dimension();
    }

    private void cachePacket(MidiNotePacket packet) {
        if(packet.player != null && packet.pos != null) {
            ArrayList<NetMidiEvent> eventList = packetCacheMap.computeIfAbsent(packet.noteServerTime, (time) -> new ArrayList<>());
            eventList.add(new NetMidiEvent(packet));
        }
    }

    @Override
    protected BlockPos getPos() {
        return this.cachedPos;
    }

    @Override
    protected ResourceKey<Level> getDimension() {
        return this.cachedDimension;
    }
}
