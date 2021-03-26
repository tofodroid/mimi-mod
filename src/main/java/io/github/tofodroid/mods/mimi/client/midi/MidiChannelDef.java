package io.github.tofodroid.mods.mimi.client.midi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.sound.midi.MidiChannel;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.midi.MidiInstrument;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MidiChannelDef {
    public static final Integer MIDI_CHANNEL_IDLE_SECONDS = 8;
    private final MidiChannel channel;
    private final Integer channelNum;

    private boolean assigned;
    private BlockPos lastNotePos;
    private Instant lastNoteTime;
    private Byte instrumentId;
    private UUID playerId;

    public MidiChannelDef(Integer channelNum, MidiChannel channel) {
        this.channelNum = channelNum;
        this.channel = channel;
        this.assigned = false;
    }

    public static MidiChannelDef unused(Integer channelNum) {
        MidiChannelDef unusedDef = new MidiChannelDef(channelNum, null);
        unusedDef.playerId = new UUID(0,0);
        return unusedDef;
    }

    public void assign(UUID playerId, MidiInstrument instrument) {
        this.assigned = true;
        this.playerId = playerId;
        this.instrumentId = instrument.getId();
        this.channel.programChange(instrument.getBank(), instrument.getPatch());
    }

    public void reset() {
        this.assigned = false;
        this.instrumentId = null;
        this.lastNotePos = null;
        this.lastNoteTime = null;
        this.playerId = null;
        this.channel.allNotesOff();
    }

    public void noteOn(MidiInstrument instrument, Byte note, Byte velocity, BlockPos notePos) {
        channel.noteOn(note, velocity);
        this.lastNotePos = notePos;
        this.lastNoteTime = Instant.now();
    }

    public void noteOff(Byte note) {
        if(MidiNoteOffPacket.ALL_NOTES_OFF.equals(note)) {
            channel.allNotesOff();
        } else { 
            channel.noteOff(note);
        }
    }

    public Boolean tick(PlayerEntity clientPlayer) {
        if(!this.assigned) {
            MIMIMod.LOGGER.warn("Attempted to tick unassigned channel: " + this.channel.toString());
            return null;
        }

        Boolean clientChannel = isClientChannel(clientPlayer.getUniqueID());

        if(isPlayerUsingInstrument(clientPlayer.getEntityWorld()) && !this.isIdle()) {
            setVolume(clientPlayer.getPosition(), clientChannel);

            if (!clientChannel) {
                setLRPan(clientPlayer.getPosition(), clientPlayer.getRotationYawHead());
            }

            return true;
        } else if(this.lastNoteTime != null) {
            return false;
        } else {
            MIMIMod.LOGGER.warn("Attempted to tick unassigned channel: " + this.channel.toString());
            return null;
        }
    }

    public Boolean isPlayerUsingInstrument(World worldIn) {
        PlayerEntity player = worldIn.getPlayerByUuid(this.playerId);

        if(player == null) {
            return false;
        }

        Byte checkId = ItemInstrument.getEntityHeldInstrumentId(player, Hand.MAIN_HAND);
        if(checkId != null && checkId.equals(this.instrumentId)) {
            return true;
        }

        checkId = ItemInstrument.getEntityHeldInstrumentId(player, Hand.OFF_HAND);
        if(checkId != null && checkId.equals(this.instrumentId)) {
            return true;
        }

        TileInstrument instrumentTile = BlockInstrument.getTileInstrumentForEntity(player);
        checkId = instrumentTile != null ? instrumentTile.getInstrumentId() : null;
        if(checkId != null && checkId.equals(this.instrumentId)) {
            return true;
        }

        return false;
    }

    public Boolean isAssigned() {
        return this.assigned;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public Byte getInstrumentId() {
        return this.instrumentId;
    }
    
    public Integer getChannelNumber() {
        return this.channelNum;
    }

    private Boolean isClientChannel(UUID clientPlayerId) {
        return this.assigned && clientPlayerId.equals(this.playerId);
    }
    
    private Boolean isIdle() {
        if(lastNoteTime != null) {
            return Math.abs(ChronoUnit.SECONDS.between(Instant.now(), lastNoteTime)) > MIDI_CHANNEL_IDLE_SECONDS;
        }
        return true;
    }
    
    @SuppressWarnings({ "resource" })
    protected Byte setVolume(BlockPos playerPos, Boolean clientChannel) {
        Double volume = 127d;

        // 1. Adjust for distance
        if(!clientChannel) {
            volume -= 0.033 * playerPos.distanceSq(lastNotePos);
        }

        // 2. Adjust for game volume
        Float catVolume = Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.PLAYERS);
              catVolume *= Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.MASTER);
              volume *= catVolume.doubleValue();

        // Clamp
        volume = volume < 0 ? 0 : volume > 127 ? 127 : volume;
        Byte byteVal = Integer.valueOf(volume.intValue()).byteValue();

        // Apply
        this.channel.controlChange(7, byteVal);
        return byteVal;
    }

    protected Byte setLRPan(BlockPos playerPos, Float playerHeadRoationYaw) {
        // Calculate
        Float posAngle = angleBetween(playerPos, lastNotePos);
        Float headAngle = playerHeadRoationYaw;
              headAngle = (headAngle < 0 ? headAngle + 360 : headAngle) % 360;
        Double relativeAngle = (posAngle.doubleValue() - headAngle.doubleValue() + 630) % 360;
        Double relVal = 64 * Math.sin(Math.toRadians(relativeAngle));
               relVal *= 0.5;

        // Clamp
        Integer lrPan = 63 + relVal.intValue();
                lrPan = lrPan < 0 ? 0 : lrPan > 127 ? 127 : lrPan;
        Byte byteVal = lrPan.byteValue();

        // Apply
        this.channel.controlChange(10, byteVal);
        return byteVal;
    }

    protected static Float angleBetween(BlockPos source, BlockPos target) {
        Float angle = (float) Math.toDegrees(Math.atan2(target.getZ() - source.getZ(), target.getX() - source.getX()));
        return (angle < 0 ? angle + 360 : angle) % 360;
    }

    public static enum MidiChannelNumber {
        ZERO,
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        ELEVEN,
        TWELVE,
        THIRTEEN,
        FOURTEEN,
        FIFTEEN
    }
}
