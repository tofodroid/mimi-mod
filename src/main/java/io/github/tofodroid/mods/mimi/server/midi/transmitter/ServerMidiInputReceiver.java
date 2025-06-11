package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.function.Consumer;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.server.ServerExecutorProxy;

public class ServerMidiInputReceiver implements Receiver {
    private volatile Boolean open = true;
    private final Consumer<ShortMessage> handler;

    public ServerMidiInputReceiver(Consumer<ShortMessage> handler) {
        this.handler = handler;
    }

    public synchronized void send(MidiMessage msg, long timeStamp) {
        if(open && msg instanceof ShortMessage) {
            ServerExecutorProxy.executeOnServerThread(() -> {
                handler.accept((ShortMessage)msg);
            });
        }
    }

    public void close() {
        open = false;
    }
}
