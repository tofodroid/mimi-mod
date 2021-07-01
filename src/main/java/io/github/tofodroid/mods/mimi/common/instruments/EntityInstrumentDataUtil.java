package io.github.tofodroid.mods.mimi.common.instruments;

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
    public void linkToMaestro(TileInstrument instrumentData, UUID playerId) {
        instrumentData.setMaestro(playerId);
    }
    
    @Override
    public UUID getLinkedMaestro(TileInstrument instrumentData) {
        return instrumentData.getMaestro();
    }

    @Override
    public Byte getInstrumentIdFromData(TileInstrument instrumentData) {
        return instrumentData.getInstrumentId();
    }

    @Override
    protected void setMidiEnabled(TileInstrument instrumentData, Boolean enabled) {
        instrumentData.setMidiEnabled(enabled);
        
    }

    @Override
    public Boolean isMidiEnabled(TileInstrument instrumentData) {
        return instrumentData.isMidiEnabled();
    }

    @Override
    public String getInstrumentName(TileInstrument instrumentData) {
        return instrumentData.getBlockState().getBlock().asItem().getName().getString();
    }
}
