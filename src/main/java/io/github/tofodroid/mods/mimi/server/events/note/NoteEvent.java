package io.github.tofodroid.mods.mimi.server.events.note;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class NoteEvent {
    public final MidiNotePacket event;
    public final UUID senderId;
    public final ResourceKey<Level> dimension;

    public NoteEvent(MidiNotePacket event, UUID senderId, ResourceKey<Level> dimension) {
        this.event = event;
        this.senderId = senderId;
        this.dimension = dimension;
    }
}
