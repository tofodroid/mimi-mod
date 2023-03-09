package io.github.tofodroid.mods.mimi.server.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.midi.MidiInputReceiver;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacketHandler;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class BroadcasterReceiver extends MidiInputReceiver {
    protected TileBroadcaster tile;

    public BroadcasterReceiver(TileBroadcaster tile) {
        super();
        this.tile = tile;
    }

    @Override
    protected void handleMessage(ShortMessage message) {
        if(!tile.isRemoved() && isNoteOnMessage(message)) {
            this.sendTransmitterNoteOnPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isNoteOffMessage(message)) {
            this.sendTransmitterNoteOffPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1]);
        } else if(isAllNotesOffMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
            //this.sendTransmitterAllNotesOffPacket(Integer.valueOf(message.getChannel()).byteValue());
        } else if(isSupportedControlMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        }
    }
    
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        TransmitterNotePacket packet = TransmitterNotePacket.createNotePacket(channel, midiNote, velocity, tile.getTransmitMode());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, tile.getBlockPos(), (ServerLevel)tile.getLevel(), tile.getMusicPlayerId(), null);
        });        
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        TransmitterNotePacket packet = TransmitterNotePacket.createNotePacket(channel, midiNote, Integer.valueOf(0).byteValue(), tile.getTransmitMode());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, tile.getBlockPos(), (ServerLevel)tile.getLevel(), tile.getMusicPlayerId(), null);
        });     
    }

    public void sendTransmitterAllNotesOffPacket(Byte channel) {
        TransmitterNotePacket packet = TransmitterNotePacket.createAllNotesOffPacket(channel, tile.getTransmitMode());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, tile.getBlockPos(), (ServerLevel)tile.getLevel(), tile.getMusicPlayerId(), null);
        });     
    }

    public void sendTransmitterControllerPacket(Byte channel, Byte controller, Byte value) {
        TransmitterNotePacket packet = TransmitterNotePacket.createControllerPacket(channel, controller, value, tile.getTransmitMode());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(packet, tile.getBlockPos(), (ServerLevel)tile.getLevel(), tile.getMusicPlayerId(), null);
        });     
    }
    
    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return false;
    }
}
