package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.forge.common.config.ModConfigs;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public abstract class AMIMISynth<T extends MIMIChannel> implements AutoCloseable {
    public static final String MC_DEFAULT_DEVICE="Device";
    private static final Integer IDEAL_CHANNELS[] = {2,1};
    private static final Integer IDEAL_BIT_RATES[] = {16, 8, 4};
    private static final Float IDEAL_SAMPLE_RATES[] = {48000.0f,44100.0f,22050.0f,16000.0f,11025.0f,8000.0f};

    protected SoftSynthesizer internalSynth;
    protected Receiver internalSynthReceiver;
    protected Boolean closing = false;
    protected final ImmutableList<T> midiChannelSet;
    protected final BiMap<T, String> channelAssignmentMap;

    public AMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds) {
        try {
            AudioFormat targetFormat = new AudioFormat(
                ModConfigs.CLIENT.synthSampleRate.get(), 
                ModConfigs.CLIENT.synthBitRate.get(), 
                2, true, false
            );

            this.internalSynth = createSynth(targetFormat, jitterCorrection, false, latency, sounds);

            if(internalSynth == null || !internalSynth.isOpen()) {
                throw new MidiUnavailableException("Synthesizer failed to open but did not produce an error.");
            }

            this.internalSynthReceiver = this.internalSynth.getReceiver();
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to initialize MIDI Synthesizer: ", e);
            this.internalSynth = null;
            this.internalSynthReceiver = null;
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
    protected abstract String createChannelId(MidiNotePacket message);
    
    @Override
    public void close() {
        closing = true;

        // Close Midi
        if(internalSynth != null && internalSynth.isOpen()) {
            this.allNotesOff();

            if(this.internalSynthReceiver != null) {
                this.internalSynthReceiver.close();
            }

            internalSynth.close();
        }
    }

    public long getSynthEventTimestamp(Long systemEventMillis) {
        Long synthOffsetMicros = this.internalSynth.getMicrosecondPosition() - Util.getEpochMillis()*1000;
        return Math.max(systemEventMillis*1000 + synthOffsetMicros, this.internalSynth.getMicrosecondPosition());
    }

    public void noteOn(MidiNotePacket message, Long timestamp) {
        if(this.channelAssignmentMap == null || this.channelAssignmentMap.isEmpty() || closing) {
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
                this.internalSynthReceiver.send(new ShortMessage(ShortMessage.NOTE_ON, channel.getChannelNumber(), message.note, message.velocity), getSynthEventTimestamp(timestamp));
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to handle note on: ", e);
            }
        }
    }

    public void noteOff(MidiNotePacket message, Long timestamp) {
        if(this.channelAssignmentMap == null || this.channelAssignmentMap.isEmpty() || closing) {
            return;
        }

        T channel = channelAssignmentMap.inverse().get(createChannelId(message));
        
        if(channel != null) {
            if(message.isAllNotesOffPacket()) {
                channel.allNotesOff();
                channel.reset();
                this.internalSynth.getMainMixer().clearQueuedChannelEvents(channel.getChannelNumber());
                channelAssignmentMap.remove(channel);
            } else {
                try {
                    this.internalSynthReceiver.send(new ShortMessage(ShortMessage.NOTE_OFF, channel.getChannelNumber(), message.note, 0), getSynthEventTimestamp(timestamp));
                } catch(Exception e) {
                    MIMIMod.LOGGER.error("Failed to handle note off: ", e);
                }
            }
        }
    }

    public void allNotesOff() {
        if(internalSynth != null ) {
            for(T channel : this.channelAssignmentMap.keySet()) {
                channel.allNotesOff();

                if(this.internalSynth != null && this.internalSynth.isOpen() && this.internalSynth.getMainMixer() != null) {
                    this.internalSynth.getMainMixer().clearQueuedChannelEvents(channel.getChannelNumber());
                }
            }
        }
    }

    public void controlChange(MidiNotePacket message, Long timestamp) {
        T channel = channelAssignmentMap.inverse().get(createChannelId(message));
        
        if(channel != null) {
            try {
                this.internalSynthReceiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel.getChannelNumber(), message.getControllerNumber(), message.getControllerValue()), getSynthEventTimestamp(timestamp));
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to handle control change. Packet: " + message.note + " | " + message.getControllerValue(), e);
            }
            channel.controlChange(message.getControllerNumber(), message.getControllerValue());
        }
    }

    public static AudioFormat getBestFormatForLine(AudioFormat targetFormat, DataLine.Info lineInfo) {
        List<AudioFormat> linesFormats = Arrays.asList(lineInfo.getFormats()).stream().filter(format -> format.getEncoding().equals(Encoding.PCM_SIGNED) || format.getEncoding().equals(Encoding.PCM_UNSIGNED)).collect(Collectors.toList());
        AudioFormat bestFormat = null;

        if(lineInfo.isFormatSupported(targetFormat)) {
            return targetFormat;
        }
        
        // Identify closest supported AudioFormat to target on this line
        for(AudioFormat format : linesFormats) {
            for(Integer channels : IDEAL_CHANNELS) {
                for(Integer bitRate : IDEAL_BIT_RATES) {
                    for(Float sampleRate : IDEAL_SAMPLE_RATES) {
                        if(format.getSampleSizeInBits() <= targetFormat.getSampleSizeInBits() && format.getSampleRate() <= targetFormat.getSampleRate())
                        if(format.getChannels() == channels || format.getChannels() == AudioSystem.NOT_SPECIFIED)
                        if(format.getSampleSizeInBits() == bitRate || format.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED)
                        if(format.getSampleRate() == sampleRate || format.getSampleRate() == AudioSystem.NOT_SPECIFIED)
                            bestFormat = format;
                    }
                }
            }
        }

        return bestFormat;
    }

    public static Pair<AudioFormat, DataLine.Info> getBestFormatLine(Mixer outputDevice, AudioFormat targetFormat) {
        AudioFormat bestFormat = null;
        DataLine.Info bestLine = null;

        if(outputDevice != null) {
            List<DataLine.Info> idealLines = Arrays.asList(outputDevice.getSourceLineInfo()).stream().filter(line -> line.getLineClass() == SourceDataLine.class).map(line -> (DataLine.Info)line).collect(Collectors.toList());
            
            for(DataLine.Info lineInfo : idealLines) {
                if(lineInfo.isFormatSupported(targetFormat)) {
                    return ImmutablePair.of(targetFormat, lineInfo);
                }

                AudioFormat bestLineFormat = getBestFormatForLine(targetFormat, lineInfo);

                if(bestLineFormat != null) {
                    if(bestFormat == null) {
                        bestFormat = bestLineFormat;
                        bestLine = lineInfo;
                    } else {
                        if(bestLineFormat.getChannels() >= bestFormat.getChannels())
                        if(bestLineFormat.getSampleSizeInBits() >= bestFormat.getSampleSizeInBits())
                        if(bestLineFormat.getSampleRate() >= bestFormat.getSampleRate()) {
                            bestFormat = bestLineFormat;
                            bestLine = lineInfo;
                        }
                    }
                }
            }

            if(bestLine != null) {
                return ImmutablePair.of(bestFormat, bestLine);
            }
        }

        MIMIMod.LOGGER.warn("Failed to find any supported Audio Output Devices. Attempting fallback to System Default.");

        return null;
    }

    private static int scanMaxChannels(Line.Info[] lines) {
        int maxChannels = 0;
        for (Line.Info line : lines) {
            if (line instanceof DataLine.Info) {
                int numChannels = scanMaxChannels(((DataLine.Info) line));
                if (numChannels > maxChannels) {
                    maxChannels = numChannels;
                }
            }
        }
        return maxChannels;
    }

    private static int scanMaxChannels(DataLine.Info info) {
        int maxChannels = 0;
        for (AudioFormat format : info.getFormats()) {
            int numChannels = format.getChannels();
            if (numChannels > maxChannels) {
                maxChannels = numChannels;
            }
        }
        return maxChannels;
    }

    @SuppressWarnings("resource")
    public static Mixer getTargetOrDefaultOutputDevice() {
        String mcDevice = Minecraft.getInstance().getSoundManager().soundEngine.library.getCurrentDeviceName();

        MIMIMod.LOGGER.info("Minecraft Audio Output Devices:\n    " + Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().stream().collect(Collectors.joining(",\n    ")));
        MIMIMod.LOGGER.info("Selected Audio Output Device: '" + mcDevice + "'");

        // Minecraft device name is "<Driver> on <Device>" and we only want device
        if(mcDevice.toLowerCase().indexOf(" on ") >= 0) {
            mcDevice = mcDevice.substring(mcDevice.toLowerCase().indexOf(" on ")+4).trim();
        }

        // If there are multiple devices with the same name, Minecraft appends "#_" to the end (1-indexed)
        Integer matchNumber = 0;
        if(mcDevice.toLowerCase().matches(".* #[0-9][0-9]*$")) {
            matchNumber = Integer.parseInt(mcDevice.substring(mcDevice.lastIndexOf("#")+1).trim())-1;
            mcDevice = mcDevice.substring(0, mcDevice.lastIndexOf("#")).trim();
        }

        MIMIMod.LOGGER.info("Searching for parsed Audio Output Device: '" + mcDevice + " #" + (matchNumber+1) + "'");

        // Identify Valid Output Devices
        List<Mixer.Info> outputDevices = new ArrayList<>();
        for(Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);

            if(scanMaxChannels(mixer.getSourceLineInfo()) > 0) {
                MIMIMod.LOGGER.info("Found Audio Output Device: " + info.toString());
                outputDevices.add(info);
            }
        }
        // Find target device
        List<Mixer.Info> matchingDevices = new ArrayList<>();
        if(mcDevice.isEmpty()) {
            // Select device based on #
            if(!outputDevices.isEmpty()) {
                if(outputDevices.size() > matchNumber) {
                    Mixer.Info info = outputDevices.get(matchNumber);
                    MIMIMod.LOGGER.info("Found Matching Device: " + info.getClass().getName() + ": " + info.toString() + " (" + matchNumber + ")");
                } else {
                    Mixer.Info info = outputDevices.get(0);
                    MIMIMod.LOGGER.warn("Expected to find at least " + matchNumber + " devices but only found " + matchingDevices.size() + ". Using first found device: " + info.getClass().getName() + ": " + info.toString() + " (System Default)");
                    matchingDevices.add(info);
                }
            }
        } else {
            // Select device based on name
            for(Mixer.Info info : outputDevices) {
                if(info.getName().equals(mcDevice)) {
                    MIMIMod.LOGGER.info("Found Matching Device: " + info.getClass().getName() + ": " + info.toString() + " (" + matchingDevices.size() + ")");
                    matchingDevices.add(info);
                }
            }
        }

        // Select Desired Matching Device
        if(matchingDevices.size() > matchNumber) {
            Mixer.Info targetInfo = matchingDevices.get(matchNumber);
            MIMIMod.LOGGER.info("Opened Target Device: " + targetInfo.getName() + " (" + matchNumber + ")");
            return AudioSystem.getMixer(matchingDevices.get(matchNumber));
        } else if(!matchingDevices.isEmpty()) {
            Mixer.Info targetInfo = matchingDevices.get(0);
            MIMIMod.LOGGER.warn("Expected to find at least " + matchNumber + " devices with name '" + mcDevice + "' but only found " + matchingDevices.size() + ". Using first found device.");
            MIMIMod.LOGGER.info("Opened Target Device: " + targetInfo.getName() + " (" + matchNumber + ")");
            return AudioSystem.getMixer(targetInfo);
        }

        // Fallback to System default
        MIMIMod.LOGGER.warn("Failed to find MC Audio Deicve '" + mcDevice + "'. Falling back to system default output device.");

        Mixer defaultMixer = null;
        if(!outputDevices.isEmpty()) {
            defaultMixer = AudioSystem.getMixer(outputDevices.get(0));
        }

        if(defaultMixer != null) {
            Mixer.Info info = defaultMixer.getMixerInfo();
            MIMIMod.LOGGER.info("Found Matching Device: " + info.getClass().getName() + ": " + info.toString() + " (System Default)");
        } else {
            MIMIMod.LOGGER.warn("Failed to find Mixer for System Default Output Device. Falling back to system provided device.");
            defaultMixer = AudioSystem.getMixer(null);
        }

        if(defaultMixer != null) {
            Mixer.Info info = defaultMixer.getMixerInfo();
            MIMIMod.LOGGER.info("Opened Target Device: " + info.getName() + " (System Default)");
        }

        return defaultMixer;
    }

    public static SoftSynthesizer createSynth(AudioFormat targetFormat, Boolean jitterCorrection, Boolean limitChannel10, Integer latency, Soundbank sounds) throws MidiUnavailableException, LineUnavailableException {
        SoftSynthesizer midiSynth = new SoftSynthesizer();

        if(midiSynth.getMaxReceivers() != 0) {
            midiSynth.open();
            midiSynth.close();
     
            Mixer outputDevice = getTargetOrDefaultOutputDevice();
            Pair<AudioFormat, DataLine.Info> bestLineFormat = getBestFormatLine(outputDevice, targetFormat);
            Map<String, Object> params = new HashMap<>();
            params.put("jitter correction", jitterCorrection);
            params.put("limit channel 10", limitChannel10);
            params.put("latency", latency * 1000);

            if(bestLineFormat != null) {
                MIMIMod.LOGGER.info("Identified suitable data line on output device: " + outputDevice.getMixerInfo().toString() + " --> " + bestLineFormat.getLeft().toString());
                params.put("format", bestLineFormat.getLeft());
                midiSynth.open((SourceDataLine)outputDevice.getLine(bestLineFormat.getRight()), params);
            } else {
                MIMIMod.LOGGER.warn("Failed to identify suitable data line on target output device. Attempting fallback.");
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
