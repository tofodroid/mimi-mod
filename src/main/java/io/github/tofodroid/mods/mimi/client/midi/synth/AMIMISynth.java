package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.List;
import java.util.ArrayList;

import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Soundbank;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;

import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;

import net.minecraft.world.entity.player.Player;

public abstract class AMIMISynth<T extends MIMIChannel> implements AutoCloseable {
    protected SoftSynthesizer internalSynth;
    protected final ImmutableList<T> midiChannelSet;
    protected final BiMap<T, String> channelAssignmentMap;

    public AMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds) {
        try {
            this.internalSynth = SoftSynthesizer.create(jitterCorrection, false, latency, ModConfigs.CLIENT.synthBitRate.get(), ModConfigs.CLIENT.synthSampleRate.get(), sounds);
        } catch(Exception e) {
            this.internalSynth = null;
        }

        if(internalSynth != null) {
            // Setup channel map
            Builder<T> builder = ImmutableList.builder();
            for(int i = 0; i < internalSynth.getChannels().length; i++) {
                builder.add(createChannel(i, this.internalSynth.getChannels()[i]));
            }
            this.midiChannelSet = builder.build();
            this.channelAssignmentMap = HashBiMap.create(midiChannelSet.size());
        } else {
            this.midiChannelSet = null;
            this.channelAssignmentMap = null;
            this.close();
        }
    }

    public abstract Boolean tick(Player clientPlayer);
    protected abstract T createChannel(Integer num, MidiChannel channel);
    protected abstract String createChannelId(MidiNotePacket message);
    
    @Override
    public void close() {
        // Close Midi
        if(internalSynth != null && internalSynth.isOpen()) {
            this.allNotesOff();
            internalSynth.close();
        }
    }

    public void noteOn(MidiNotePacket message) {
        InstrumentSpec instrument = InstrumentConfig.getBydId(message.instrumentId);
        T channel = channelAssignmentMap.inverse().get(createChannelId(message));

        if(channel == null) {
            List<T> remainingChannels = new ArrayList<T>(Lists.newArrayList(midiChannelSet));
            remainingChannels.removeAll(channelAssignmentMap.keySet());
            if(!remainingChannels.isEmpty()) {
                channel = remainingChannels.get(0);
                channel.setInstrument(instrument);
                channelAssignmentMap.put(channel, createChannelId(message));
            }
        }

        if(channel != null) {
            channel.noteOn(instrument, message.note, message.velocity, message.pos);
        }
    }

    public void noteOff(MidiNotePacket message) {
        InstrumentSpec instrument = InstrumentConfig.getBydId(message.instrumentId);
        T channel = channelAssignmentMap.inverse().get(createChannelId(message));
        
        if(channel != null) {
            if(message.isAllNotesOffPacket()) {
                channel.allNotesOff();
            } else {
                channel.noteOff(instrument, message.note);
            }
        }
    }

    public void allNotesOff() {
        for(T channel : this.channelAssignmentMap.keySet()) {
            channel.allNotesOff();
        }
    }

    public void controlChange(MidiNotePacket message) {
        T channel = channelAssignmentMap.inverse().get(createChannelId(message));
        
        if(channel != null) {
            channel.controlChange(message.getControllerNumber(), message.getControllerValue());
        }
    }
}
