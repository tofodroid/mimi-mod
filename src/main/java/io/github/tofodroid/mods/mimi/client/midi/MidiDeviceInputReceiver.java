package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrumentContainerScreen;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class MidiDeviceInputReceiver extends MidiInputReceiver {
    @Override
    protected void handleMessage(ShortMessage message, PlayerEntity player) {
        MIMIMod.proxy.getMidiInput().getLocalInstrumentsForMidiDevice(player, new Integer(message.getChannel()).byteValue()).forEach(instrument -> {
            if(isNoteOnMessage(message)) {
                handleMidiNoteOn(new Integer(message.getChannel()).byteValue(), instrument, message.getMessage()[1], message.getMessage()[2], player);
            } else if(isNoteOffMessage(message)) {
                handleMidiNoteOff(new Integer(message.getChannel()).byteValue(), instrument, message.getMessage()[1], player);
            } else if(isAllNotesOffMessage(message)) {
                handleMidiNoteOff(new Integer(message.getChannel()).byteValue(), instrument, MidiNoteOffPacket.ALL_NOTES_OFF, player);
            }
        });
    }
    
    @SuppressWarnings("resource")
    public void handleMidiNoteOn(Byte channel, Byte instrument, Byte midiNote, Byte velocity, PlayerEntity player) {
        MidiNoteOnPacket packet = new MidiNoteOnPacket(channel, midiNote, velocity, instrument, player.getUniqueID(), player.getPosition());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handleNoteOn(packet);

        if(Minecraft.getInstance().currentScreen instanceof GuiInstrumentContainerScreen && shouldShowOnGUI(player, packet.channel, packet.instrumentId)) {
            ((GuiInstrumentContainerScreen)Minecraft.getInstance().currentScreen).onMidiNoteOn(packet.channel, packet.note, packet.velocity);
        }
    }
    
    @SuppressWarnings("resource")
    public void handleMidiNoteOff(Byte channel, Byte instrument, Byte midiNote, PlayerEntity player) {
        MidiNoteOffPacket packet = new MidiNoteOffPacket(channel, midiNote, instrument, player.getUniqueID());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handleNoteOff(packet);

        if(Minecraft.getInstance().currentScreen instanceof GuiInstrumentContainerScreen && shouldShowOnGUI(player, packet.channel, packet.instrumentId)) {
            ((GuiInstrumentContainerScreen)Minecraft.getInstance().currentScreen).onMidiNoteOff(packet.channel, packet.note);
        }
    }
    
    public static Boolean shouldShowOnGUI(PlayerEntity player, Byte channel, Byte instrument) {    
        if(player.openContainer instanceof ContainerInstrument) {
            ItemStack switchStack = ((ContainerInstrument)player.openContainer).getSelectedSwitchboard();
            Byte guiInstrument = ((ContainerInstrument)player.openContainer).getInstrumentId();

            if(instrument == guiInstrument && ModItems.SWITCHBOARD.equals(switchStack.getItem())) {
                if(ItemMidiSwitchboard.isChannelEnabled(switchStack, channel)) {
                    return true;
                }             
            }
        }

        return false;
    }
}
