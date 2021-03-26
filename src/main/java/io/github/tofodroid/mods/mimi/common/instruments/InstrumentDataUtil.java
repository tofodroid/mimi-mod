package io.github.tofodroid.mods.mimi.common.instruments;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.util.SortedArraySet;

public abstract class InstrumentDataUtil<T extends Object> {
    public static final String MAESTRO_TAG = "maestro_uuid";
    public static final String INPUT_MODE_TAG = "input_mode";
    public static final String LISTEN_CHANNELS_TAG = "listen_channels";
    protected static final String ALL_CHANNELS_STRING = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16";

    abstract protected void setInputMode(T instrumentData, Integer inputMode);
    abstract protected void setAcceptedChannelString(T instrumentData, String acceptedChannelsString);
    abstract protected String getAcceptedChannelsString(T instrumentData);
    
    abstract public void linkToMaestro(T instrumentData, UUID playerId);
    abstract public UUID getLinkedMaestro(T instrumentData);

    //abstract public void linkToSpeaker(T instrumentData, BlockPos speakerPos);
    //abstract public BlockPos getConnectedSpeaker(T instrumentData);
    abstract public Integer getInputMode(T instrumentData);
    abstract public Byte getInstrumentIdFromData(T instrumentData);

    public void toggleChannel(T instrumentData, Byte channelId) {
        if(channelId != null && channelId < 16 && channelId >= 0) {
            SortedArraySet<Byte> acceptedChannels = getAcceptedChannelsSet(instrumentData);

            if(acceptedChannels == null) {
                acceptedChannels = SortedArraySet.newSet(16);
            }

            if(doesAcceptChannel(instrumentData, channelId)) {
                acceptedChannels.remove(channelId);
            } else {
                acceptedChannels.add(channelId);
            }

            String acceptedChannelsString = acceptedChannels.stream().map(b -> new Integer(b + 1).toString()).collect(Collectors.joining(","));
            setAcceptedChannelString(instrumentData, acceptedChannelsString);
        }
    }
    
    public void clearAcceptedChannels(T instrumentData) {
        setAcceptedChannelString(instrumentData, null);
    }
    
    public void setAcceptAllChannels(T instrumentData) {
        setAcceptedChannelString(instrumentData, ALL_CHANNELS_STRING);
    }

    public Boolean doesAcceptChannel(T instrumentData, Byte channelId) {
        return getAcceptedChannelsSet(instrumentData).contains(channelId);
    }
    
    public SortedArraySet<Byte> getAcceptedChannelsSet(T instrumentData) {
        String acceptedChannelString = getAcceptedChannelsString(instrumentData);

        if(acceptedChannelString != null && !acceptedChannelString.isEmpty()) {
            SortedArraySet<Byte> result = SortedArraySet.newSet(16);
            result.addAll(Arrays.asList(acceptedChannelString.split(",", -1)).stream().map(b -> new Integer(Byte.valueOf(b) - 1).byteValue()).collect(Collectors.toSet()));
            return result;
        }

        return SortedArraySet.newSet(0);
    }
    
    public Boolean relayInputSelected(T instrumentData) {
        return Integer.valueOf(1).equals(getInputMode(instrumentData));
    }
    
    public Boolean midiInputSelected(T instrumentData) {
        return Integer.valueOf(2).equals(getInputMode(instrumentData));
    }


    public void cycleInputMode(T instrumentData, Boolean midiDevicesExist) {
        Integer selectedInputMode = getInputMode(instrumentData);
        if(selectedInputMode == null) selectedInputMode = 0;

        switch(selectedInputMode) {
            case 0:
                if(getLinkedMaestro(instrumentData) != null) {
                    setInputMode(instrumentData, 1);
                } else if(midiDevicesExist) {
                    setInputMode(instrumentData, 2);
                }
                break;
            case 1:
                if(midiDevicesExist) {
                    setInputMode(instrumentData, 2);
                } else {
                    setInputMode(instrumentData, 0);
                }
                break;
            case 2:
            default:
                setInputMode(instrumentData, 0);
                break;
        }
    }

    public String getSelectedInputString(T instrumentData) {
        Integer inputMode = getInputMode(instrumentData);

        if(new Integer(1).equals(inputMode)) {
            return "Maestro";
        } else if(new Integer(2).equals(inputMode)) {
            return "MIDI";
        }

        return "None";
    }
}
