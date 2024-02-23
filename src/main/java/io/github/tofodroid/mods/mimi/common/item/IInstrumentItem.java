package io.github.tofodroid.mods.mimi.common.item;

public interface IInstrumentItem extends IColorableItem {
    public Byte getInstrumentId();
    public Integer getDefaultChannels();
    public String getRegistryName();
}
