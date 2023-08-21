package io.github.tofodroid.mods.mimi.server.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.midi.AMidiInputReceiver;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacketHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TransmitterReceiver extends AMidiInputReceiver {
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
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isSupportedControlMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        }
    }
    
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        TransmitterNotePacket packet = TransmitterNotePacket.createNotePacket(channel, midiNote, velocity);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.level(), player.getUUID());
        });        
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        TransmitterNotePacket packet = TransmitterNotePacket.createNotePacket(channel, midiNote, Integer.valueOf(0).byteValue());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.level(), player.getUUID());
        });     
    }

    public void sendTransmitterAllNotesOffPacket(Byte channel) {
        TransmitterNotePacket packet = TransmitterNotePacket.createAllNotesOffPacket(channel);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.level(), player.getUUID());
        });     
    }

    public void sendTransmitterControllerPacket(Byte channel, Byte controller, Byte value) {
        TransmitterNotePacket packet = TransmitterNotePacket.createControllerPacket(channel, controller, value);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, player.getOnPos(), (ServerLevel)player.level(), player.getUUID());
        });     
    }
    
    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return false;
    }
}
