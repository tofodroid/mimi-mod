package io.github.tofodroid.mods.mimi.common.data;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

public class EntityInstrumentDataUtil extends InstrumentDataUtil<TileInstrument> {
    public static final EntityInstrumentDataUtil INSTANCE = new EntityInstrumentDataUtil();

    @Override
    protected void setAcceptedChannelString(TileInstrument instrumentData, String acceptedChannelsString) {
        instrumentData.setAcceptedChannelsString(acceptedChannelsString);
    }

    @Override
    protected String getAcceptedChannelsString(TileInstrument instrumentData) {
        return instrumentData.getAcceptedChannelsString();
    }

    @Override
    public void setMidiSource(TileInstrument instrumentData, UUID sourceId) {
        instrumentData.setMaestro(sourceId);
    }
    
    @Override
    public UUID getMidiSource(TileInstrument instrumentData) {
        return instrumentData.getMaestro();
    }

    @Override
    public Byte getInstrumentIdFromData(TileInstrument instrumentData) {
        return instrumentData.getInstrumentId();
    }

    @Override
    public String getInstrumentName(TileInstrument instrumentData) {
        return instrumentData.getBlockState().getBlock().asItem().getName().getString();
    }
}
