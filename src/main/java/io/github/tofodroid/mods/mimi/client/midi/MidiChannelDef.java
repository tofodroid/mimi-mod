package io.github.tofodroid.mods.mimi.client.midi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.sound.midi.MidiChannel;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import io.github.tofodroid.mods.mimi.util.DebugUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MidiChannelDef {
    public static final Integer MIDI_CHANNEL_IDLE_SECONDS = 8;
    private final MidiChannel channel;
    private final Integer channelNum;

    private boolean assigned;
    private BlockPos lastNotePos;
    private Instant lastNoteTime;
    private Byte instrumentId;
    private Boolean mechanical;
    private UUID entityId;

    public MidiChannelDef(Integer channelNum, MidiChannel channel) {
        this.channelNum = channelNum;
        this.channel = channel;
        this.assigned = false;
    }

    public void assign(UUID entityId, Boolean mechanical, InstrumentSpec instrument) {
        this.assigned = true;
        this.entityId = entityId;
        this.mechanical = mechanical;
        this.instrumentId = instrument.instrumentId;
        this.channel.programChange(instrument.midiBankNumber, instrument.midiPatchNumber);
        this.channel.controlChange(7, 0);
    }

    public void reset() {
        this.assigned = false;
        this.mechanical = false;
        this.instrumentId = null;
        this.lastNotePos = null;
        this.lastNoteTime = null;
        this.entityId = null;
        this.channel.allNotesOff();
    }

    public void noteOn(InstrumentSpec instrument, Byte note, Byte velocity, BlockPos notePos) {
        channel.noteOn(note, velocity);
        this.lastNotePos = notePos;
        this.lastNoteTime = Instant.now();
        DebugUtils.logNoteTimingInfo(this.getClass(), true, instrumentId, note, velocity, notePos);
    }

    public void noteOff(Byte note) {
        if(MidiNotePacket.ALL_NOTES_OFF.equals(note)) {
            channel.allNotesOff();
        } else { 
            channel.noteOff(note);
        }
        DebugUtils.logNoteTimingInfo(this.getClass(), false, instrumentId, note, null, null);
    }

    public Boolean tick(Player clientPlayer) {
        if(!this.assigned) {
            MIMIMod.LOGGER.warn("Attempted to tick unassigned channel: " + this.channel.toString());
            return null;
        } else {
            Boolean clientChannel = isClientChannel(clientPlayer.getUUID());
            if(!this.isIdle() && !(!clientChannel && Math.sqrt(clientPlayer.getOnPos().distSqr(lastNotePos)) > 72d) && ((!mechanical && isPlayerUsingInstrument(clientPlayer.getLevel()) || (mechanical/* && isMechanicalMaestroExisting(clientPlayer.getLevel())*/)))) {
                if (!clientChannel) {
                    setVolume(Math.sqrt(clientPlayer.getOnPos().distSqr(lastNotePos)));
                    setLRPan(clientPlayer.getOnPos(), clientPlayer.getYHeadRot());
                } else {
                    setVolume(0d);
                }

                return true;
            } else if(this.lastNoteTime != null) {
                return false;
            } else {
                MIMIMod.LOGGER.warn("Attempted to tick unassigned channel: " + this.channel.toString());
                return null;
            }
        }
    }

    public Boolean isPlayerUsingInstrument(Level worldIn) {
        Player player = worldIn.getPlayerByUUID(this.entityId);

        if(player == null) {
            return false;
        }

        Byte checkId = ItemInstrument.getEntityHeldInstrumentId(player, InteractionHand.MAIN_HAND);
        if(checkId != null && checkId.equals(this.instrumentId)) {
            return true;
        }

        checkId = ItemInstrument.getEntityHeldInstrumentId(player, InteractionHand.OFF_HAND);
        if(checkId != null && checkId.equals(this.instrumentId)) {
            return true;
        }

        TileInstrument instrumentTile = BlockInstrument.getTileInstrumentForEntity(player);
        checkId = instrumentTile != null ? instrumentTile.getInstrumentId() : null;
        if(checkId != null && checkId.equals(this.instrumentId) && (lastNotePos != null ? lastNotePos.equals(player.getOnPos()) : true)){
            return true;
        }

        return false;
    }

    public Boolean isMechanicalMaestroExisting(Level worldIn) {
        BlockEntity tile = worldIn.getBlockEntity(this.lastNotePos);
        TileMechanicalMaestro mech = tile != null && ModTiles.MECHANICALMAESTRO.equals(tile.getType()) ? (TileMechanicalMaestro) tile : null;

        return mech != null && EntityNoteResponsiveTile.entityExists(worldIn, Double.valueOf(this.lastNotePos.getX()), Double.valueOf(this.lastNotePos.getY()), Double.valueOf(this.lastNotePos.getZ()));
    }
    
    public Integer getChannelNumber() {
        return this.channelNum;
    }

    private Boolean isClientChannel(UUID clientPlayerId) {
        return this.assigned && clientPlayerId.equals(this.entityId);
    }
    
    private Boolean isIdle() {
        if(lastNoteTime != null) {
            return Math.abs(ChronoUnit.SECONDS.between(Instant.now(), lastNoteTime)) > MIDI_CHANNEL_IDLE_SECONDS;
        }
        return true;
    }
    
    @SuppressWarnings({ "resource" })
    protected Byte setVolume(Double distance) {

        // 1. Adjust for distance
        Double volume = 127d - Math.floor((127 * Math.pow(distance,2.5)) / (Math.pow(distance,2.5) + Math.pow(72 - distance,2.5)));

        // 2. Adjust for game volume
        Float catVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.PLAYERS);
              catVolume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
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
}
