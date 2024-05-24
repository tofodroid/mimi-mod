package io.github.tofodroid.mods.mimi.common.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

public class BasicMidiEvent {
    public static final Byte ALL_CHANNELS = Byte.MAX_VALUE;
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;

    public final MidiEventType type;
    public final Byte channel;
    public final Byte note;
    public final Byte velocity;
    public final Long eventTime;

    public BasicMidiEvent(BasicMidiEvent source) {
        this(source.type, source.channel, source.note, source.velocity, source.eventTime);
    }

    public BasicMidiEvent(MidiEventType type, Byte channel, Byte note, Byte velocity, Long eventTime) {
        this.type = type;
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.eventTime = eventTime;
    }

    public BasicMidiEvent(ShortMessage message) {
        this.channel = Integer.valueOf(message.getChannel()).byteValue();
        this.note = Integer.valueOf(message.getData1()).byteValue();
        this.velocity = Integer.valueOf(message.getData2()).byteValue();
        this.eventTime = MIMIMod.getProxy().getCurrentServerMillis();

        if(isNoteOffMessage(message)) {
            this.type = MidiEventType.NOTE_OFF;
        } else if(isNoteOnMessage(message)) {
            this.type = MidiEventType.NOTE_ON;
        } else if(isAllNotesOffMessage(message)) {
            this.type = MidiEventType.ALL_NOTES_OFF;
        } else if(isControlMessage(message)) {
            this.type = MidiEventType.CONTROL;
        } else {
            this.type = MidiEventType.OTHER;
        }
    }

    public static BasicMidiEvent allNotesOff(Byte channel, Long eventTime) {
        return new BasicMidiEvent(MidiEventType.ALL_NOTES_OFF, channel, BasicMidiEvent.ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), eventTime);
    }

    public static BasicMidiEvent allNotesOff(Long eventTime) {
        return new BasicMidiEvent(MidiEventType.ALL_NOTES_OFF, BasicMidiEvent.ALL_CHANNELS, BasicMidiEvent.ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), eventTime);
    }
    
    protected Boolean isNoteOnMessage(ShortMessage msg) {
        return msg.getData1() >= 0 && ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() > 0;
    }

    protected Boolean isNoteOffMessage(ShortMessage msg) {
        return msg.getData1() >= 0 && (ShortMessage.NOTE_OFF == msg.getCommand() || (ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() == 0));
    }

    protected Boolean isAllNotesOffMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && ( msg.getData1() == 120 || msg.getData1() == 123);
    }

    protected Boolean isControlMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && !( msg.getData1() == 120 || msg.getData1() == 123);
    }

    public Byte getControllerNumber() {
        return type == MidiEventType.CONTROL ? Integer.valueOf(-this.note).byteValue() : null;
    }

    public Byte getControllerValue() {
        return type == MidiEventType.CONTROL ? this.velocity : null;
    }
}
