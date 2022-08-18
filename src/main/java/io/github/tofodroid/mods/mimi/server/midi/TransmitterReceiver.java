package io.github.tofodroid.mods.mimi.server.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputReceiver;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacketHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TransmitterReceiver extends MidiInputReceiver {
    protected Player player;

    public TransmitterReceiver(Player player) {
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
            this.sendTransmitterAllNotesOffPacket(Integer.valueOf(message.getChannel()).byteValue());
        } else if(isSupportedControlMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        }
    }
    
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        TransmitterNotePacket packet = new TransmitterNotePacket(channel, midiNote, velocity, ItemTransmitter.getTransmitMode(ServerMusicPlayerMidiManager.getTransmitterStack(player.getUUID())));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.getLevel(), player.getUUID(), null);
        });        
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        TransmitterNotePacket packet = new TransmitterNotePacket(channel, midiNote, Integer.valueOf(0).byteValue(), ItemTransmitter.getTransmitMode(ServerMusicPlayerMidiManager.getTransmitterStack(player.getUUID())));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.getLevel(), player.getUUID(), null);
        });     
    }

    public void sendTransmitterAllNotesOffPacket(Byte channel) {
        TransmitterNotePacket packet = TransmitterNotePacket.createAllNotesOffPacket(channel, ItemTransmitter.getTransmitMode(ServerMusicPlayerMidiManager.getTransmitterStack(player.getUUID())));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.getLevel(), player.getUUID(), null);
        });     
    }

    public void sendTransmitterControllerPacket(Byte channel, Byte controller, Byte value) {
        TransmitterNotePacket packet = TransmitterNotePacket.createControllerPacket(channel, controller, value, ItemTransmitter.getTransmitMode(ServerMusicPlayerMidiManager.getTransmitterStack(player.getUUID())));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.getLevel(), player.getUUID(), null);
        });     
    }
    
    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return false;
    }
}
