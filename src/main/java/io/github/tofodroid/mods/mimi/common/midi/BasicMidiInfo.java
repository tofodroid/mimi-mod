package io.github.tofodroid.mods.mimi.common.midi;

import java.util.UUID;

public class BasicMidiInfo {
    public final String fileName;
    public final UUID fileId;
    public final Boolean serverMidi;

    public BasicMidiInfo(String fileName, UUID fileId, Boolean serverMidi) {
        this.fileName = fileName.length() > 100 ? fileName.substring(0, 100) : fileName;
        this.fileId = fileId;
        this.serverMidi = serverMidi;
    }
}
