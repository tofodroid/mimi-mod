package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputReceiver;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;

// Receiver
public class MidiFileCasterInputReceiver extends MidiInputReceiver {
    @Override
    protected void handleMessage(ShortMessage message) {
        if(MIMIMod.proxy.isClient() && ((ClientProxy)MIMIMod.proxy).getMidiInput().fileCasterIsActive()) {
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
    }
    
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        if(MIMIMod.proxy.isClient()) {
            TransmitterNotePacket packet = new TransmitterNotePacket(channel, midiNote, velocity, ((ClientProxy)MIMIMod.proxy).getMidiInput().getTransmitMode());
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        if(MIMIMod.proxy.isClient()) {
            TransmitterNotePacket packet = new TransmitterNotePacket(channel, midiNote, Integer.valueOf(0).byteValue(), ((ClientProxy)MIMIMod.proxy).getMidiInput().getTransmitMode());
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }

    public void sendTransmitterAllNotesOffPacket(Byte channel) {
        if(MIMIMod.proxy.isClient()) {
            TransmitterNotePacket packet = TransmitterNotePacket.createAllNotesOffPacket(channel, ((ClientProxy)MIMIMod.proxy).getMidiInput().getTransmitMode());
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }

    public void sendTransmitterControllerPacket(Byte channel, Byte controller, Byte value) {
        if(MIMIMod.proxy.isClient()) {
            TransmitterNotePacket packet = TransmitterNotePacket.createControllerPacket(channel, controller, value, ((ClientProxy)MIMIMod.proxy).getMidiInput().getTransmitMode());
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }
    
    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return false;
    }
}
