package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.sampled.AudioFormat;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;

import io.github.tofodroid.mods.mimi.common.midi.MidiChannelNumber;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.util.DebugUtils;

import net.minecraft.world.entity.player.Player;

public abstract class AMIMISynth<T extends MIMIChannel> implements AutoCloseable {
    protected final SoftSynthesizer internalSynth;
    protected final ImmutableList<T> midiChannelSet;
    protected final BiMap<T, String> channelAssignmentMap;

    public AMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds) {
        this.internalSynth = openNewSynth(jitterCorrection, latency, sounds);

        if(internalSynth != null) {
            // Setup channel map
            Builder<T> builder = ImmutableList.builder();
            for(MidiChannelNumber num : MidiChannelNumber.values()) {
                builder.add(createChannel(num.ordinal(), this.internalSynth.getChannels()[num.ordinal()]));
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

    public final void noteOn(MidiNotePacket message) {
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

        channel.noteOn(instrument, message.note, message.velocity, message.pos);
    }

    public final void noteOff(MidiNotePacket message) {
        InstrumentSpec instrument = InstrumentConfig.getBydId(message.instrumentId);
        T channel = channelAssignmentMap.inverse().get(createChannelId(message));

        if(channel != null) {
            channel.noteOff(instrument, message.note);
        }
    }

    public final void allNotesOff() {
        for(T channel : this.channelAssignmentMap.keySet()) {
            channel.noteOff(null, MidiNotePacket.ALL_NOTES_OFF);
        }
    }
    
    public void channelNotesOff(MidiChannelNumber num) {
        if(num != null) {
            this.midiChannelSet.get(num.ordinal()).noteOff(null, MidiNotePacket.ALL_NOTES_OFF);
        }
    }

    protected SoftSynthesizer openNewSynth(Boolean jitterCorrection, Integer latency, Soundbank sounds) {
        try {
            SoftSynthesizer midiSynth = new SoftSynthesizer();

            if(midiSynth.getMaxReceivers() != 0) {
                midiSynth.open();
                midiSynth.close();

                Map<String, Object> params = new HashMap<>();
                params.put("jitter correction", ModConfigs.CLIENT.jitterCorrection.get());
                params.put("latency", ModConfigs.CLIENT.latency.get() * 1000);
                params.put("format", new AudioFormat(
                    ModConfigs.CLIENT.synthSampleRate.get(), 
                    ModConfigs.CLIENT.synthBitRate.get(), 
                    2, true, false
                ));

                DebugUtils.logSynthInfo(midiSynth, params);
                
                midiSynth.open(null, params);
                
                if(sounds != null) {
                    if(midiSynth.isSoundbankSupported(sounds)) {
                        midiSynth.loadAllInstruments(sounds);
                    } else {
                        MIMIMod.LOGGER.error("Synthesizer could not load Soundbank. Falling back to default.");
                    }
                }
                
                midiSynth.getReceiver();
                
                return midiSynth;
            }

            throw new MidiUnavailableException("Midi Synth '" + midiSynth.getDeviceInfo().getName() + "' cannot support any receivers.");
        } catch(MidiUnavailableException e) {
            MIMIMod.LOGGER.error(e);
        }

        return null;
    }
}
