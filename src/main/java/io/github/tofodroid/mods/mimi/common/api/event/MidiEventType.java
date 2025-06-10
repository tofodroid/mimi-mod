package io.github.tofodroid.mods.mimi.common.api.event;

import javax.sound.midi.ShortMessage;

public enum MidiEventType {
    NOTE_ON,
    NOTE_OFF,
    CONTROL,
    RESET,
    PITCH_BEND,
    OTHER,
    UNKNOWN;

    public Byte toByte() {
        return MidiEventType.toByte(this);
    }

    public static Byte toByte(MidiEventType val) {
        return (byte)val.ordinal();
    }

    public static MidiEventType fromByte(Byte val) {
        MidiEventType[] values = MidiEventType.values();
        if(val >= 0 && val < values.length) {
            return values[val];
        }
        return UNKNOWN;
    }
    
    public static MidiEventType fromShortMessage(ShortMessage message) {
        if(isNoteOffMessage(message)) {
            return MidiEventType.NOTE_OFF;
        } else if(isNoteOnMessage(message)) {
            return MidiEventType.NOTE_ON;
        } else if(isSupportedControlMessage(message)) {
            return MidiEventType.CONTROL;
        } else if(isPitchBendMessage(message)) {
            return MidiEventType.PITCH_BEND;
        }
        return MidiEventType.OTHER;
    }

    protected static Boolean isNoteOnMessage(ShortMessage msg) {
        return msg.getData1() >= 0 && ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() > 0;
    }

    protected static Boolean isNoteOffMessage(ShortMessage msg) {
        return msg.getData1() >= 0 && (ShortMessage.NOTE_OFF == msg.getCommand() || (ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() == 0));
    }

    protected static Boolean isSupportedControlMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && (
            msg.getData1() == 1 // Modulation
            || msg.getData1() == 2 // Breath Modulation
            || msg.getData1() == 4 // Foot Controller
            || msg.getData1() == 120 // All sound off
            || msg.getData1() == 121 // Reset controllers
            || msg.getData1() == 123 // All notes off
            || (msg.getData1() >= 64 && msg.getData1() <= 69) // Sustain Pedal
            || msg.getData1() == 84 // Portamento
            || (msg.getData1() >= 91 && msg.getData1() <= 95) // Effects
        );
    }

    protected static Boolean isPitchBendMessage(ShortMessage msg) {
        return ShortMessage.PITCH_BEND == msg.getCommand();
    }
}
