package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.Transmitter;

public abstract class MidiInputSourceManager {
    protected Transmitter activeTransmitter = null;

    protected abstract void openTransmitter();

    public void close() {
        if(activeTransmitter != null) {
            activeTransmitter.close();
            activeTransmitter = null;
        }
    }
}
