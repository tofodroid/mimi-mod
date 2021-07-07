package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

public abstract class InstrumentDataUpdatePacket {
    public static final UUID NULL_MAESTRO_VAL = new UUID(0,0);

    public final String acceptedChannelString;
    public final UUID maestroId;

    public InstrumentDataUpdatePacket(UUID maestroId, String acceptedChannelString) {
        this.maestroId = maestroId;
        this.acceptedChannelString = acceptedChannelString;
    }
}
