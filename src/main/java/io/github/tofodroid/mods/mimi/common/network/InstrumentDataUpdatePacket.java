package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

public abstract class InstrumentDataUpdatePacket {
    public static final UUID NULL_MAESTRO_VAL = new UUID(0,0);

    public final Boolean midiEnabled;
    public final String acceptedChannelString;
    public final UUID maestroId;

    public InstrumentDataUpdatePacket(UUID maestroId, Boolean midiEnabled, String acceptedChannelString) {
        this.maestroId = maestroId;
        this.midiEnabled = midiEnabled;
        this.acceptedChannelString = acceptedChannelString;
    }
}
