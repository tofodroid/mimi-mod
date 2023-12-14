package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.midi.AMidiInputReceiver;
import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.server.midi.receiver.ServerMusicReceiverManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerTransmitterMidiReceiver extends AMidiInputReceiver {
    protected Player player;

    public PlayerTransmitterMidiReceiver(Player player) {
        super();
        this.player = player;
    }

    @Override
    protected void handleMessage(ShortMessage message) {
        if(isNoteOnMessage(message)) {
            this.sendTransmitterNoteOnPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isNoteOffMessage(message)) {
            this.sendTransmitterNoteOffPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1]);
        } else if(isAllNotesOffMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isSupportedControlMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        }
    }
    
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        TransmitterNoteEvent packet = TransmitterNoteEvent.createNoteEvent(channel, midiNote, velocity);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, player.getUUID(), player.getOnPos(), (ServerLevel)player.level());
        });
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        TransmitterNoteEvent packet = TransmitterNoteEvent.createNoteEvent(channel, midiNote, Integer.valueOf(0).byteValue());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, player.getUUID(), player.getOnPos(), (ServerLevel)player.level());
        });
    }

    public void sendTransmitterAllNotesOffPacket(Byte channel) {
        TransmitterNoteEvent packet = TransmitterNoteEvent.createAllNotesOffEvent(channel);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, player.getUUID(), player.getOnPos(), (ServerLevel)player.level());
        });
    }

    public void sendTransmitterControllerPacket(Byte channel, Byte controller, Byte value) {
        TransmitterNoteEvent packet = TransmitterNoteEvent.createControllerEvent(channel, controller, value);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, player.getUUID(), player.getOnPos(), (ServerLevel)player.level());
        });
    }
    
    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return false;
    }
}
