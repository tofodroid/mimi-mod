package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public abstract class MidiInputReceiver implements Receiver {
    @SuppressWarnings("resource")
    public void send(MidiMessage msg, long timeStamp) {
        Player player = Minecraft.getInstance().player;

        if(player != null && msg instanceof ShortMessage) {
            handleMessage((ShortMessage)msg, player);
        }
    }

    public void close() { }

    protected abstract void handleMessage(ShortMessage message, Player player);

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
