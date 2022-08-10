package io.github.tofodroid.mods.mimi.common.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public abstract class MidiInputReceiver implements Receiver {
    private volatile boolean open = true;

    public synchronized void send(MidiMessage msg, long timeStamp) {
        if(open && msg instanceof ShortMessage) {
            handleMessage((ShortMessage)msg);
        }
    }
    
    protected abstract void handleMessage(ShortMessage message);

    public void close() {
        open = false;
    }

    // Message Utils
    protected Boolean isNoteOnMessage(ShortMessage msg) {
        return ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() > 0;
    }

    protected Boolean isNoteOffMessage(ShortMessage msg) {
        return ShortMessage.NOTE_OFF == msg.getCommand() || (ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() == 0);
    }

    protected Boolean isAllNotesOffMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && ( msg.getData1() == 120 || msg.getData1() == 123);
    }
    
    protected abstract Boolean isSupportedControlMessage(ShortMessage msg);
}
