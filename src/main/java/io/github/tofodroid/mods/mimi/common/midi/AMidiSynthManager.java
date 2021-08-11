package io.github.tofodroid.mods.mimi.common.midi;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;

public abstract class AMidiSynthManager implements AutoCloseable {
    
    public abstract void init();

    @Override
    public abstract void close();

    public abstract void handlePacket(MidiNotePacket message);
    
    public abstract Boolean shouldShowOnGUI(UUID messagePlayer, Byte channel, Byte instrument);

    public abstract void allNotesOff();
    
    public abstract void allNotesOff(MidiChannelNumber num);

    public abstract void handleTick(PlayerTickEvent event);

    public abstract void handleSelfLogOut(LoggedOutEvent event);
    
    public abstract void handleOtherLogOut(PlayerLoggedOutEvent event);
    
}
