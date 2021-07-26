package io.github.tofodroid.mods.mimi.common.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;

public class ReceiverDataUtil extends CommonDataUtil<TileReceiver> {
    public static final ReceiverDataUtil INSTANCE = new ReceiverDataUtil();

    @Override
    public void setAcceptedChannelString(TileReceiver instrumentData, String acceptedChannelsString) {
        instrumentData.setAcceptedChannelsString(acceptedChannelsString);
    }

    @Override
    public String getAcceptedChannelsString(TileReceiver instrumentData) {
        return instrumentData.getAcceptedChannelsString(); 
    }

    @Override
    public void setMidiSource(TileReceiver instrumentData, UUID sourceId) {
        instrumentData.setMidiSource(sourceId);       
    }

    @Override
    public UUID getMidiSource(TileReceiver instrumentData) {
       return instrumentData.getMidiSource();
    }

    public Boolean shouldHandleMessage(TileReceiver instrumentData, UUID sender, Byte channel, Boolean publicTransmit, Byte note) {
        return super.shouldHandleMessage(instrumentData, sender, channel, publicTransmit) && shouldPlayNote(instrumentData, note);
    }

    public Boolean shouldPlayNote(TileReceiver instrumentData, Byte note) {
        Boolean result =  true;
        ArrayList<Byte> allowedNotes = getFilterNotes(instrumentData);

        if(allowedNotes != null && !allowedNotes.isEmpty()) {
            result = allowedNotes.contains(note);
        }

        return result;
    }

    public void setFilterNoteString(TileReceiver instrumentData, String filterString) {
        instrumentData.setFilterNoteString(filterString);
    }

    public String getFilterNoteString(TileReceiver instrumentData) {
        return instrumentData.getFilterNoteString();
    }
    
    public ArrayList<Byte> getFilterNotes(TileReceiver instrumentData) {
        String filterString = getFilterNoteString(instrumentData);

        if(filterString != null && !filterString.isEmpty()) {
            ArrayList<Byte> result = new ArrayList<>();
            result.addAll(Arrays.asList(filterString.split(",", -1)).stream().map(b -> new Integer(Byte.valueOf(b)).byteValue()).collect(Collectors.toSet()));
            return result;
        }

        return new ArrayList<>();
    }
}
