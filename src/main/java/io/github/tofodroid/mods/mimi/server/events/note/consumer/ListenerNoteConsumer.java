package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import io.github.tofodroid.mods.mimi.server.events.note.NoteEvent;
import io.github.tofodroid.mods.mimi.server.events.note.api.ANoteConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ListenerNoteConsumer extends ANoteConsumer {
    private AConfigurableMidiNoteResponsiveTile tile;

    public ListenerNoteConsumer(TileListener tile) {
        super(tile.getUUID(), tile.getFilteredInstrument());
        this.tile = tile;
    }

    @Override
    protected Boolean doHandleNoteOn(NoteEvent message) {
        tile.onNoteOn(null, message.event.note, message.event.velocity, message.event.instrumentId, message.event.noteServerTime);
        return null;
    }
    
    @Override
    protected Boolean doHandleNoteOff(NoteEvent message) {
        tile.onNoteOff(null, message.event.note, message.event.velocity, message.event.instrumentId, message.event.noteServerTime);
        return null;
    }

    @Override
    protected Boolean doHandleAllNotesOff(NoteEvent message) {
        tile.onAllNotesOff(null, null, message.event.noteServerTime);
        return null;
    }

    @Override
    protected Boolean willHandleNoteOn(NoteEvent message) {
        return tile.shouldTriggerFromNoteOn(null, message.event.note, message.event.velocity, message.event.instrumentId);
    }

    @Override
    protected Boolean willHandleNoteOff(NoteEvent message) {
        return tile.shouldTriggerFromNoteOff(null, message.event.note, message.event.velocity, message.event.instrumentId);
    }

    @Override
    protected Boolean willHandleAllNotesOff(NoteEvent message) {
        return tile.shouldTriggerFromAllNotesOff(null, message.event.instrumentId);
    }

    @Override
    protected Boolean willHandleControl(NoteEvent message) {
        return false;
    }

    @Override
    protected Boolean doHandleControl(NoteEvent message) {
        return false;
    }

    @Override
    protected BlockPos getPos() {
        return tile.getBlockPos();
    }

    @Override
    protected ResourceKey<Level> getDimension() {
        return tile.getLevel().dimension();
    }
}
