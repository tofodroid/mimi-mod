package io.github.tofodroid.mods.mimi.common.network;

import java.io.Serializable;

public class SerializedMidiFileInfo implements Serializable  {
    public final String fileName;        
    public final byte[] channelMapping;
    public final Integer songLengthSeconds;
    public final Integer tempoBpm;

    public SerializedMidiFileInfo(String fileName, byte[] channelMapping, Integer songLengthSeconds, Integer tempoBpm) {
        this.fileName = fileName;
        this.channelMapping = channelMapping;
        this.songLengthSeconds = songLengthSeconds;
        this.tempoBpm = tempoBpm;
    }
}
