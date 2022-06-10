package io.github.tofodroid.mods.mimi.common.midi;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

public abstract class AMidiSynthManager implements AutoCloseable {
    public abstract void close();
    public abstract void handlePacket(MidiNotePacket message);
    public abstract void handleLocalPacket(MidiNotePacket message);
    public abstract void allNotesOff();
    public abstract void handleTick(PlayerTickEvent event);
    public abstract void handleSelfLogOut(LoggedOutEvent event);
}
