package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.function.Consumer;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.midi.BasicMidiEvent;
import io.github.tofodroid.mods.mimi.server.ServerExecutor;


public class ServerMidiInputReceiver implements Receiver {
    private volatile Boolean open = true;
    private final Consumer<BasicMidiEvent> handler;

    public ServerMidiInputReceiver(Consumer<BasicMidiEvent> handler) {
        this.handler = handler;
    }

    public synchronized void send(MidiMessage msg, long timeStamp) {
        if(open && msg instanceof ShortMessage) {
            BasicMidiEvent event = new BasicMidiEvent((ShortMessage)msg);
            ServerExecutor.executeOnServerThread(() -> {
                handler.accept(event);
            });
        }
    }

    public void close() {
        open = false;
    }
}
