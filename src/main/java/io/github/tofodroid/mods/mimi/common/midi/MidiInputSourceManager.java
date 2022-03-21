package io.github.tofodroid.mods.mimi.common.midi;

import javax.sound.midi.Transmitter;

public abstract class MidiInputSourceManager {
    protected Transmitter activeTransmitter = null;

    protected abstract void openTransmitter();
    public abstract void open();

    public void close() {
        if(activeTransmitter != null) {
            activeTransmitter.setReceiver(null);
            activeTransmitter.close();
            activeTransmitter = null;
        }
    }
}
