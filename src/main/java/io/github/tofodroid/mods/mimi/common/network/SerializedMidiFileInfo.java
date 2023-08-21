package io.github.tofodroid.mods.mimi.common.network;

import java.io.Serializable;

public class SerializedMidiFileInfo implements Serializable  {
    public final String fileName;        
    public final byte[] channelMapping;
    public final Integer songLengthSeconds;

    public SerializedMidiFileInfo(String fileName, byte[] channelMapping, Integer songLengthSeconds) {
        this.fileName = fileName;
        this.channelMapping = channelMapping;
        this.songLengthSeconds = songLengthSeconds;
    }
}
