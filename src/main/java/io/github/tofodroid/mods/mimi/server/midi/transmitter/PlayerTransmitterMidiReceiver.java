package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.UUID;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.server.midi.AServerMidiInputReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class PlayerTransmitterMidiReceiver extends AServerMidiInputReceiver {
    protected Player player;

    public PlayerTransmitterMidiReceiver(Player player) {
        super();
        this.player = player;
    }

    @Override
    protected void handleMessage(ShortMessage message) {
        // Try to find new instance of player if died
        if(this.player.isDeadOrDying()) {
            this.player = this.player.level().getPlayerByUUID(this.player.getUUID());
        }

        if(this.player.isDeadOrDying()) {
            return;
        }

        if(isNoteOnMessage(message)) {
            this.sendTransmitterNoteOnPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isNoteOffMessage(message)) {
            this.sendTransmitterNoteOffPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1]);
        } else if(isAllNotesOffMessage(message)) {
            this.sendTransmitterControllerPacket(Integer.valueOf(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        }
    }
    
    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return false;
    }

    @Override
    protected UUID getTransmitterId() {
        return player.getUUID();
    }

    @Override
    protected BlockPos getTransmitterPos() {
        return player.getOnPos();
    }

    @Override
    protected ServerLevel getTransmitterLevel() {
        return (ServerLevel)player.level();
    }
}
