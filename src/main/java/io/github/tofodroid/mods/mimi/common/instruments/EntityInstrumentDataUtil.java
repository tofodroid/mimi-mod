package io.github.tofodroid.mods.mimi.common.instruments;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

public class EntityInstrumentDataUtil extends InstrumentDataUtil<TileInstrument> {
    public static final EntityInstrumentDataUtil INSTANCE = new EntityInstrumentDataUtil();

    @Override
    protected void setInputMode(TileInstrument instrumentData, Integer inputMode) {
        instrumentData.setInputMode(inputMode);
    }

    @Override
    protected void setAcceptedChannelString(TileInstrument instrumentData, String acceptedChannelsString) {
        instrumentData.setAcceptedChannelsString(acceptedChannelsString);
    }

    @Override
    protected String getAcceptedChannelsString(TileInstrument instrumentData) {
        return instrumentData.getAcceptedChannelsString();
    }

    /*
    @Override
    public void linkToSpeaker(TileInstrument instrumentData, BlockPos speakerPos) {
        instrumentData.setLinkedSpeaker(speakerPos);
    }

    @Override
    public BlockPos getConnectedSpeaker(TileInstrument instrumentData) {
        return instrumentData.getLinkedSpeaker();
    }
    */

    @Override
    public void linkToMaestro(TileInstrument instrumentData, UUID playerId) {
        instrumentData.setMaestro(playerId);
    }
    
    @Override
    public UUID getLinkedMaestro(TileInstrument instrumentData) {
        return instrumentData.getMaestro();
    }

    @Override
    public Integer getInputMode(TileInstrument instrumentData) {
        return instrumentData.getInputMode();
    }

    @Override
    public Byte getInstrumentIdFromData(TileInstrument instrumentData) {
        return instrumentData.getInstrumentId();
    }
}
