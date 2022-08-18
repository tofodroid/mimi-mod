package io.github.tofodroid.mods.mimi.common.network;

public abstract class ServerMidiStatus {
    public enum STATUS_CODE {
        SUCCESS,
        EMPTY,
        ERROR_URL,
        ERROR_HOST,
        ERROR_DISABLED,
        ERROR_OTHER,
        ERROR_NOT_FOUND,
        UNKNOWN;

        public static STATUS_CODE fromByte(byte b) {
            try {
                return STATUS_CODE.values()[b];
            } catch(Exception e) {}
            return STATUS_CODE.UNKNOWN;
        }
    };
}
