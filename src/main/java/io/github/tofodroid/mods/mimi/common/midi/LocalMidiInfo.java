package io.github.tofodroid.mods.mimi.common.midi;

import java.io.File;
import java.util.UUID;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.util.MidiFileUtils;

public class LocalMidiInfo {
    public final Boolean serverFile;
    public final File file;
    public final UUID fileId;

    private LocalMidiInfo(File file, UUID fileId, Boolean serverFile) {
        this.serverFile = serverFile;
        this.file = file;
        this.fileId = fileId;
    }

    public UUID getFileId() {
        return this.fileId;
    }

    public Sequence loadSequenceFromFile() {
        try {
            return MidiSystem.getSequence(this.file);
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to load sequence: " + this.file.getAbsolutePath() + " - " + e.getMessage());
            return null;
        }
    }

    public BasicMidiInfo getBasicMidiInfo() {
        return new BasicMidiInfo(file.getName(), fileId, serverFile);
    }

    @Override
    public boolean equals(Object other) {
        if(other == null || !(other instanceof LocalMidiInfo)) {
            return false;
        }
        return ((LocalMidiInfo)other).getFileId().equals(this.getFileId());
    }

    public static LocalMidiInfo fromFile(File file, Boolean isServer) {
        if(file.exists()) {
            try {
                return new LocalMidiInfo(file, LocalMidiInfo.createFileId(file), isServer);
            } catch(Exception e) {
                MIMIMod.LOGGER.warn("Invalid MIDI file: " + file.getAbsolutePath());
            }
        }
        return null;
    }

    public static UUID createFileId(File file) {
        try {
            String channelString = "";
            Sequence sequence = MidiSystem.getSequence(file);
            byte[] byteChannelMapping = MidiFileUtils.getChannelMapping(sequence);
            for(int i = 0; i < byteChannelMapping.length; i++) {
                channelString += Integer.valueOf(byteChannelMapping[i]).toString();
            } 
            return UUID.nameUUIDFromBytes(new StringBuilder()
                .append("file:" + file.getName().trim() + ";")
                .append("tempo:" + MidiFileUtils.getTempoBPM(sequence) + ";")
                .append("length:" + MidiFileUtils.getSongLenghtSeconds(sequence) + ";")
                .append("channels:" + channelString + ";")
                .toString().getBytes());
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to create file ID for file: " + file.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }        
    }
}