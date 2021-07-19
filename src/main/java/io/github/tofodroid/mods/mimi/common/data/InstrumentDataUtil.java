package io.github.tofodroid.mods.mimi.common.data;

public abstract class InstrumentDataUtil<T extends Object> extends CommonDataUtil<T> {
    abstract public Byte getInstrumentIdFromData(T instrumentData);
    abstract public String getInstrumentName(T instrumentData);
}
