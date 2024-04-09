package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.forge.common.config.ModConfigs;
import net.minecraft.client.Minecraft;

public class AudioOutputDeviceManager {
    private static final Integer IDEAL_CHANNELS[] = {2,1};
    private static final Integer IDEAL_BIT_RATES[] = {16, 8, 4};
    private static final Float IDEAL_SAMPLE_RATES[] = {48000.0f,44100.0f,22050.0f,16000.0f,11025.0f,8000.0f};

    private Mixer currentDevice = null;
    private Pair<String, Integer> targetDeviceId = ImmutablePair.of(null, 0);
    private AudioFormat currentFormat = null;
    private DataLine.Info currentLine = null;

    public AudioOutputDeviceManager() {
        this.refreshDevice();
    }

    public Pair<AudioFormat, SourceDataLine> getOutputFormatLine() {
        if(currentDevice != null && currentLine != null) {
            try {
                return ImmutablePair.of(currentFormat, AudioSystem.getSourceDataLine(currentFormat, currentDevice.getMixerInfo()));
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to open target data line on target device: ", e);
                this.currentFormat = null;
                this.currentLine = null;
            }
        }
        return ImmutablePair.of(null,null);
    }
    
    public void refreshDevice() {
        this.currentLine = null;
        this.currentFormat = null;
        this.targetDeviceId = getTargetDeviceId();
        this.currentDevice = getTargetDevice();

        if(currentDevice != null) {
            AudioFormat targetFormat = new AudioFormat(
                ModConfigs.CLIENT.synthSampleRate.get(), 
                ModConfigs.CLIENT.synthBitRate.get(), 
                2, true, false
            );

            Pair<AudioFormat, DataLine.Info> outputLineInfo = getBestFormatLine(currentDevice, targetFormat);

            if(outputLineInfo.getLeft() != null && outputLineInfo.getRight() != null) {
                this.currentLine = outputLineInfo.getRight();
                this.currentFormat = outputLineInfo.getLeft();
            }
        }
    }

    public void setAutomaticDevice() {
        ModConfigs.CLIENT.automaticAudioDevice.set(true);
        ModConfigs.CLIENT.audioOutputDevice.set("");
        refreshDevice();
    }

    public void setDevice(String deviceName) {
        ModConfigs.CLIENT.automaticAudioDevice.set(false);
        ModConfigs.CLIENT.audioOutputDevice.set(deviceName.trim());
        refreshDevice();
    }

    public String getCurrentDeviceName() {
        if(ModConfigs.CLIENT.automaticAudioDevice.get()) {
            return "Auto: " + (this.targetDeviceId.getLeft().isBlank() ? "Default" : this.targetDeviceId.getLeft() + (this.targetDeviceId.getRight() > 0 ? " #" + (this.targetDeviceId.getRight()+1) : ""));
        } else {
            return "Set: " + ModConfigs.CLIENT.audioOutputDevice.get();
        }
    }

    public String getCurrentDeviceStatus() {
        if(ModConfigs.CLIENT.automaticAudioDevice.get()) {
            if(currentDevice != null) {
                if(currentLine == null) {
                    return "Device encountered an error. Outputting to system-provided device.";
                } else if(this.targetDeviceId.getLeft().isBlank() || currentDevice.getMixerInfo().getName().equals(this.targetDeviceId.getLeft())) {
                    return "Connected.";
                } else {
                    return "Failed to connect to device. Outputting to system-provided device.";
                }
            } else {
                return "Failed to find device. Outputting to system-provided device.";
            }
        } else {
            if(currentDevice != null) {
                if(currentLine == null) {
                    return "Device encountered an error. Outputting to system-provided device.";
                } else {
                    return "Connected.";
                }
            } else {
                return "Failed to find device. Outputting to system-provided device.";
            }
        }
    }

    public Mixer getTargetDevice() {
        if(this.targetDeviceId.getLeft() != null) {
            return getOutputDeviceByNameAndNumber(this.targetDeviceId.getLeft(), this.targetDeviceId.getRight());
        } else {
            return null;
        }
    }

    public static Pair<String, Integer> getTargetDeviceId() {
        String targetDeviceName = null;
        Integer targetDeviceMatchNumber = 0;

        if(ModConfigs.CLIENT.automaticAudioDevice.get()) {
            targetDeviceName = getMinecraftOutputDeviceName();
            
            // If there are multiple devices with the same name Minecraft appends a 1-indexed number to the end
            if(targetDeviceName.toLowerCase().matches(".* #[0-9][0-9]*$")) {
                targetDeviceMatchNumber = Integer.parseInt(targetDeviceName.substring(targetDeviceName.lastIndexOf("#")+1).trim())-1;
                targetDeviceName = targetDeviceName.substring(0, targetDeviceName.lastIndexOf("#")).trim();
            }
        } else if(!ModConfigs.CLIENT.audioOutputDevice.get().isBlank()) {
            targetDeviceName = ModConfigs.CLIENT.audioOutputDevice.get().trim();
            
            // If there are multiple devices with the same name MIMI appends a 1-indexed number to the end too :)
            if(targetDeviceName.toLowerCase().matches(".* #[0-9][0-9]*$")) {
                targetDeviceMatchNumber = Integer.parseInt(targetDeviceName.substring(targetDeviceName.lastIndexOf("#")+1).trim())-1;
                targetDeviceName = targetDeviceName.substring(0, targetDeviceName.lastIndexOf("#")).trim();
            }
        }

        return ImmutablePair.of(targetDeviceName, targetDeviceMatchNumber);
    }

    public List<Mixer> getAvailableOutputDevices() {
        List<Mixer> outputDevices = new ArrayList<>();
        for(Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);

            if(scanMaxChannels(mixer.getSourceLineInfo()) > 0) {
                outputDevices.add(mixer);
            }
        }
        return outputDevices;
    }
    
    public Mixer getOutputDeviceByNameAndNumber(String deviceName, Integer matchNumber) {
        // Identify Valid Output Devices
        List<Mixer> outputDevices = getAvailableOutputDevices();

        // Find target device
        List<Mixer> matchingDevices = new ArrayList<>();

        if(deviceName.isEmpty()) {
            // Select device based on # because name is blank
            if(!outputDevices.isEmpty()) {
                if(outputDevices.size() > matchNumber) {
                    Mixer device = outputDevices.get(matchNumber);
                    MIMIMod.LOGGER.info("Opened Target Device: " + device.getMixerInfo().getName() + " (" + matchNumber + ")");
                    return device;
                } else {
                    Mixer device = outputDevices.get(0);
                    MIMIMod.LOGGER.warn("Expected to find at least " + matchNumber + " devices but only found " + matchingDevices.size() + ". Using first found device (System Default).");
                    MIMIMod.LOGGER.info("Opened Target Device: " + device.getMixerInfo().getName() + " (" + matchNumber + ")");
                    return device;
                }
            }
        } else {
            // Select device based on name
            for(Mixer device : outputDevices) {
                if(device.getMixerInfo().getName().equals(deviceName)) {
                    matchingDevices.add(device);
                }
            }
        }

        // Select Desired Matching Device
        if(matchingDevices.size() > matchNumber) {
            Mixer target = matchingDevices.get(matchNumber);
            MIMIMod.LOGGER.info("Opened Target Device: " + target.getMixerInfo().getName() + " (" + matchNumber + ")");
            return target;
        } else if(!matchingDevices.isEmpty()) {
            Mixer target = matchingDevices.get(0);
            MIMIMod.LOGGER.warn("Expected to find at least " + matchNumber + " devices with name '" + deviceName + "' but only found " + matchingDevices.size() + ". Using first found device.");
            MIMIMod.LOGGER.info("Opened Target Device: " + target.getMixerInfo().getName() + " (" + matchNumber + ")");
            return target;
        }

        return null;
    }

    @SuppressWarnings("resource")
    private static String getMinecraftOutputDeviceName() {
        String mcDevice = Minecraft.getInstance().getSoundManager().soundEngine.library.getCurrentDeviceName();

        // Minecraft device name is "<Driver> on <Device>" and we only want device
        if(mcDevice.toLowerCase().indexOf(" on ") >= 0) {
            mcDevice = mcDevice.substring(mcDevice.toLowerCase().indexOf(" on ")+4).trim();
        }

        return mcDevice;
    }
    
    public static List<String> getDeviceDisplayNames(List<Mixer> devices) {
        HashMap<String,Integer> nameCounter = new HashMap<>();
        List<String> outputNames = new ArrayList<>();

        for(Mixer device : devices) {
            Integer count = nameCounter.computeIfAbsent(device.getMixerInfo().getName(), (m) -> 0) + 1;
            nameCounter.put(device.getMixerInfo().getName(), count);
            outputNames.add(device.getMixerInfo().getName() + (count > 1 ? (" #" + count) : ""));
        }

        return outputNames;
    }    

    private static AudioFormat getBestFormatForLine(AudioFormat targetFormat, DataLine.Info lineInfo) {
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

    private static Pair<AudioFormat, DataLine.Info> getBestFormatLine(Mixer outputDevice, AudioFormat targetFormat) {
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
}
