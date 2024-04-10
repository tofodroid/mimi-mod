package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.server.events.note.NoteEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class ANoteConsumer {
    public static final Byte ALL_INSTRUMENTS_ID = -1;

    protected UUID id;
    protected Byte instrumentId;

    public ANoteConsumer(UUID id, Byte instrumentId) {
        this.id = id;
        this.instrumentId = instrumentId;
    }

    protected Boolean isPacketInRange(NoteEvent event) {
        return Math.abs(Math.sqrt(event.event.pos.distSqr(getPos()))) <= (event.event.isNoteOffPacket() ? 32 : 64) && event.dimension.equals(getDimension());
    }

    public Boolean handleNoteOn(NoteEvent message) {
        if(willHandleNoteOn(message) && isPacketInRange(message)) {
            return this.doHandleNoteOn(message);
        }
        return null;
    }

    public Boolean handleNoteOff(NoteEvent message) {
        if(willHandleNoteOff(message) && isPacketInRange(message)) {
            return this.doHandleNoteOff(message);
        }
        return null;
    }

    public Boolean handleAllNotesOff(NoteEvent message) {
        if(willHandleAllNotesOff(message) && isPacketInRange(message)) {
            return this.doHandleAllNotesOff(message);
        }
        return null;
    }

    public Boolean handleControl(NoteEvent message) {
        if(willHandleControl(message) && isPacketInRange(message)) {
            return this.doHandleControl(message);
        }
        return null;
    }
    
    public void tick() {/* Default no-op */}

    protected abstract BlockPos getPos();
    protected abstract ResourceKey<Level> getDimension();
    protected abstract Boolean willHandleNoteOn(NoteEvent message);
    protected abstract Boolean doHandleNoteOn(NoteEvent message);
    protected abstract Boolean willHandleNoteOff(NoteEvent message);
    protected abstract Boolean doHandleNoteOff(NoteEvent message);
    protected abstract Boolean willHandleAllNotesOff(NoteEvent message);
    protected abstract Boolean doHandleAllNotesOff(NoteEvent message);
    protected abstract Boolean willHandleControl(NoteEvent message);
    protected abstract Boolean doHandleControl(NoteEvent message);
}
