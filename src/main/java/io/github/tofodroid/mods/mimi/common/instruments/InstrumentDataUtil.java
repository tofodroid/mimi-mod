package io.github.tofodroid.mods.mimi.common.instruments;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.util.SortedArraySet;

public abstract class InstrumentDataUtil<T extends Object> {
    public static final String MAESTRO_TAG = "maestro_uuid";
    public static final String MIDI_ENABLED_TAG = "midi_enabled";
    public static final String LISTEN_CHANNELS_TAG = "listen_channels";
    public static final UUID MIDI_MAESTRO_ID = new UUID(0,1);
    protected static final String ALL_CHANNELS_STRING = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16";

    abstract protected void setMidiEnabled(T instrumentData, Boolean enabled);
    abstract public Boolean isMidiEnabled(T instrumentData);

    abstract protected void setAcceptedChannelString(T instrumentData, String acceptedChannelsString);
    abstract protected String getAcceptedChannelsString(T instrumentData);
    
    abstract public void linkToMaestro(T instrumentData, UUID playerId);
    abstract public UUID getLinkedMaestro(T instrumentData);

    abstract public Byte getInstrumentIdFromData(T instrumentData);

    public Boolean shouldHandleMessage(T instrumentData, UUID sender, Byte channel) {
        return isMidiEnabled(instrumentData) && sender.equals(getLinkedMaestro(instrumentData)) && doesAcceptChannel(instrumentData, channel);
    }

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
    
    public void toggleMidiEnabled(T instrumentData) {
        setMidiEnabled(instrumentData, !isMidiEnabled(instrumentData));
    }
}
