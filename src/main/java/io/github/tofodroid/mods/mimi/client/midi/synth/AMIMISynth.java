package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public abstract class AMIMISynth<T extends MIMIChannel> implements AutoCloseable {
    public static final String MC_DEFAULT_DEVICE="Device";
    protected SoftSynthesizer internalSynth;
    protected AudioFormat format;
    protected final ImmutableList<T> midiChannelSet;
    protected final BiMap<T, String> channelAssignmentMap;

    public AMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds) {
        try {
            this.format = new AudioFormat(
                ModConfigs.CLIENT.synthSampleRate.get(), 
                ModConfigs.CLIENT.synthBitRate.get(), 
                2, true, false
            );
            this.internalSynth = createSynth(getDeviceOutLine(this.format), this.format, jitterCorrection, false, latency, sounds);
            
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

    @SuppressWarnings("resource")
    public static SourceDataLine getDeviceOutLine(AudioFormat format) {
        String mcDevice = Minecraft.getInstance().options.soundDevice().get();

        // Minecraft device name is "<Driver> on <Device>" and we only want device
        if(mcDevice.toLowerCase().indexOf(" on ") >= 0) {
            mcDevice = mcDevice.substring(mcDevice.toLowerCase().indexOf(" on ")+4);
        }

        if(StringUtils.isBlank(mcDevice) || mcDevice.equals(MC_DEFAULT_DEVICE)) {
            return null;
        } else {
            try {
                for(Mixer.Info info : AudioSystem.getMixerInfo()) {
                    if(info.getClass().getName().contains("DirectAudioDevice") 
                        && info.getName().equals(mcDevice)
                    ) {
                        return AudioSystem.getSourceDataLine(format, info);
                    }
                }
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to open target AudioOut Device. Falling back to default. Error: ", e);
            }
        }

        return null;
    }

    /* Code copied from com.sun.media.sound.SoftSynthesizer and customzied */
    public static SoftSynthesizer createSynth(SourceDataLine outLine, AudioFormat format, Boolean jitterCorrection, Boolean limitChannel10, Integer latency, Soundbank sounds) throws MidiUnavailableException {
        SoftSynthesizer midiSynth = new SoftSynthesizer();

        if(midiSynth.getMaxReceivers() != 0) {
            midiSynth.open();
            midiSynth.close();

            Map<String, Object> params = new HashMap<>();
            params.put("jitter correction", jitterCorrection);
            params.put("limit channel 10", limitChannel10);
            params.put("latency", latency * 1000);
            params.put("format", format);

            midiSynth.open(outLine, params);
            
            if(sounds != null) {
                if(midiSynth.isSoundbankSupported(sounds)) {
                    midiSynth.loadAllInstruments(sounds);
                }
            }
            
            midiSynth.getReceiver();
            
            return midiSynth;
        }

        throw new MidiUnavailableException("Midi Synth '" + midiSynth.getDeviceInfo().getName() + "' cannot support any receivers.");
    }
}
