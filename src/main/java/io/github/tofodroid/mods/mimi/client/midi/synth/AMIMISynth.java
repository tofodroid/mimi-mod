package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.network.NoteEventPacket;
import io.github.tofodroid.mods.mimi.util.TimeUtils;
import net.minecraft.world.entity.player.Player;

public abstract class AMIMISynth<T extends MIMIChannel> implements AutoCloseable {
    protected SoftSynthesizer internalSynth;
    protected Receiver internalSynthReceiver;
    protected Boolean closing = false;
    protected final ImmutableList<T> midiChannelSet;
    protected final BiMap<T, String> channelAssignmentMap;

    public AMIMISynth(AudioFormat format, SourceDataLine dataLine, Boolean jitterCorrection, Integer latency, Soundbank sounds) {
        try {
            this.internalSynth = createSynth(format, dataLine, jitterCorrection, false, latency, sounds);

            if(internalSynth == null || !internalSynth.isOpen()) {
                throw new MidiUnavailableException("Synthesizer failed to open but did not produce an error.");
            }

            this.internalSynthReceiver = this.internalSynth.getReceiver();
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to initialize MIDI Synthesizer: ", e);
            this.internalSynth = null;
            this.internalSynth.getVoiceStatus();
        }

        Builder<T> builder = ImmutableList.builder();

        if(internalSynth != null) {
            // Setup channel map
            for(int i = 0; i < internalSynth.getChannels().length; i++) {
                builder.add(createChannel(i, this.internalSynth.getChannels()[i]));
            }
            this.midiChannelSet = builder.build();
            this.channelAssignmentMap = HashBiMap.create(midiChannelSet.size());
        } else {
            this.midiChannelSet = builder.build();
            this.channelAssignmentMap = HashBiMap.create(0);
            this.close();
        }
    }

    public abstract Boolean tick(Player clientPlayer);
    protected abstract T createChannel(Integer num, MidiChannel channel);
    protected abstract String createChannelId(NoteEventPacket message);
    
    @Override
    public void close() {
        closing = true;

        // Close Midi
        if(internalSynth != null && internalSynth.isOpen()) {
            this.reset();

            if(this.internalSynthReceiver != null) {
                this.internalSynthReceiver.close();
            }

            internalSynth.close();
        }
    }

    public long getSynthEventTimestamp(Long systemEventMillis) {
        Long synthOffsetMicros = this.internalSynth.getMicrosecondPosition() - TimeUtils.getNowTime()*1000;
        return Math.max(systemEventMillis*1000 + synthOffsetMicros, this.internalSynth.getMicrosecondPosition());
    }

    public void noteOn(NoteEventPacket message, Long timestamp) {
        if(this.channelAssignmentMap == null || closing) {
            return;
        }

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
            try {
                channel.noteOn(message.pos);
                this.internalSynthReceiver.send(
                    new ShortMessage(
                        ShortMessage.NOTE_ON,
                        channel.getChannelNumber(),
                        message.data1,
                        message.data2
                    ),
                    getSynthEventTimestamp(timestamp)
                );
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to handle note on: ", e);
            }
        }
    }

    public void noteOff(NoteEventPacket message, Long timestamp) {
        if(this.channelAssignmentMap == null || closing) {
            return;
        }

        T channel = channelAssignmentMap.inverse().get(createChannelId(message));

        if(channel != null) {
            try {
                this.internalSynthReceiver.send(new ShortMessage(ShortMessage.NOTE_OFF, channel.getChannelNumber(), message.data1, 0), getSynthEventTimestamp(timestamp));
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to handle note off: ", e);
            }
        }
    }

    public void reset() {
        if(internalSynth != null ) {
            for(T channel : this.channelAssignmentMap.keySet()) {
                channel.reset();

                if(this.internalSynth != null && this.internalSynth.isOpen() && this.internalSynth.getMainMixer() != null) {
                    this.internalSynth.getMainMixer().clearQueuedChannelEvents(channel.getChannelNumber());
                }
            }
        }
    }

    public void controlChange(NoteEventPacket message, Long timestamp) {
        T channel = channelAssignmentMap.inverse().get(createChannelId(message));
        
        if(channel != null) {
            try {
                MIMIMod.LOGGER.info("Control change: " + message.data1 + " | " + message.data2);
                this.internalSynthReceiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel.getChannelNumber(), message.data1, message.data2), getSynthEventTimestamp(timestamp));
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to handle control change. Packet: " + message.data1 + " | " + message.data2, e);
            }
        }
    }

    public void pitchBend(NoteEventPacket message, Long timestamp) {
        T channel = channelAssignmentMap.inverse().get(createChannelId(message));
        
        if(channel != null) {
            try {
                MIMIMod.LOGGER.info("pitch bend: " + message.data1 + " | " + message.data2);
                this.internalSynthReceiver.send(new ShortMessage(ShortMessage.PITCH_BEND, channel.getChannelNumber(), message.data1, message.data2), getSynthEventTimestamp(timestamp));
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to handle pitch bend. Packet: " + message.data1 + " | " + message.data2, e);
            }
        }
    }

    public static SoftSynthesizer createSynth(AudioFormat format, SourceDataLine dataLine, Boolean jitterCorrection, Boolean limitChannel10, Integer latency, Soundbank sounds) throws MidiUnavailableException, LineUnavailableException {
        SoftSynthesizer midiSynth = new SoftSynthesizer();

        if(midiSynth.getMaxReceivers() != 0) {
            midiSynth.open();
            midiSynth.close();
     
            Map<String, Object> params = new HashMap<>();
            params.put("jitter correction", jitterCorrection);
            params.put("limit channel 10", limitChannel10);
            params.put("latency", latency * 1000);

            if(format != null && dataLine != null) {
                MIMIMod.LOGGER.info("Opened data line on device: " + format.toString());
                params.put("format", format);
                midiSynth.open(dataLine, params);
            } else {
                MIMIMod.LOGGER.warn("Opening fallback device.");
                midiSynth.open(null, params);
            }
            
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
