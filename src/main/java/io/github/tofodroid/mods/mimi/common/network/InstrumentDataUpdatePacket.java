package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

public abstract class InstrumentDataUpdatePacket {
    public static final Integer NULL_INPUT_MODE_VAL = 0;
    public static final UUID NULL_MAESTRO_VAL = new UUID(0,0);

    public final Integer inputMode;
    public final String acceptedChannelString;
    public final UUID maestroId;

    public InstrumentDataUpdatePacket(UUID maestroId, Integer inputMode, String acceptedChannelString) {
        this.maestroId = maestroId;
        this.inputMode = inputMode;
        this.acceptedChannelString = acceptedChannelString;
    }
}
