package io.github.tofodroid.mods.mimi.common.midi;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

public class TransmitterNoteEvent {
    public static final Byte ALL_CHANNELS = Byte.MAX_VALUE;
    private static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final @Nonnull Byte channel;
    public final @Nonnull Byte note;
    public final @Nonnull Byte velocity;
    public final @Nonnull Long noteServerTime;
    
    public static TransmitterNoteEvent createNoteEvent(Byte channel, Byte note, Byte velocity) {
        return new TransmitterNoteEvent(channel, note, velocity, MIMIMod.getProxy().getCurrentServerMillis());
    }

    public static TransmitterNoteEvent createAllNotesOffEvent(Byte channel) {
        return new TransmitterNoteEvent(channel, ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), MIMIMod.getProxy().getCurrentServerMillis());
    }
    
    public static TransmitterNoteEvent createControllerEvent(Byte channel, Byte controller, Byte value) {
        return new TransmitterNoteEvent(channel, Integer.valueOf(-controller).byteValue(), value, MIMIMod.getProxy().getCurrentServerMillis());
    }

    @SuppressWarnings("null")
    private TransmitterNoteEvent(Byte channel, Byte note, Byte velocity, Long noteServerTime) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.noteServerTime = noteServerTime;
    }

    public Boolean isAllNotesOffEvent() {
        return this.note == ALL_NOTES_OFF;
    }

    public Boolean isNoteOffEvent() {
        return !isControlEvent() && velocity <= 0;
    }

    public Boolean isControlEvent() {
        return this.note < 0 && !isAllNotesOffEvent();
    }

    public Byte getControllerNumber() {
        return isControlEvent() ? Integer.valueOf(-this.note).byteValue() : null;
    }

    public Byte getControllerValue() {
        return isControlEvent() ? this.velocity : null;
    }
}
