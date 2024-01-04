package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.UUID;
import javax.sound.midi.ShortMessage;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.AServerMidiInputReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class TileTransmitterMidiReceiver extends AServerMidiInputReceiver {
    protected TileTransmitter tile;

    public TileTransmitterMidiReceiver(TileTransmitter tile) {
        super();
        this.tile = tile;
    }

    @Override
    protected void handleMessage(ShortMessage message) {
        if(isNoteOnMessage(message)) {
            this.sendTransmitterNoteOnPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isNoteOffMessage(message)) {
            this.sendTransmitterNoteOffPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1]);
        } else if(isAllNotesOffMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isSupportedControlMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        }
    }
    
    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return false;
    }

    @Override
    protected UUID getTransmitterId() {
        return tile.getUUID();
    }

    @Override
    protected BlockPos getTransmitterPos() {
        return tile.getBlockPos();
    }

    @Override
    protected ServerLevel getTransmitterLevel() {
        return (ServerLevel)tile.getLevel();
    }
}
