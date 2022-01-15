package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.github.tofodroid.com.sun.media.sound.SF2SoundbankReader;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Soundbank;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.midi.AMidiSynthManager;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class MidiMultiSynthManager extends AMidiSynthManager {
    private static final Integer MIDI_TICK_FREQUENCY = 4;

    protected Soundbank soundbank = null;
    protected Integer midiTickCounter = 0;
    protected LocalPlayerMIMISynth localSynth;
    protected MechanicalMaestroMIMISynth mechSynth;
    protected ServerPlayerMIMISynth playerSynth;

    public MidiMultiSynthManager() {
        this.soundbank = openSoundbank(ModConfigs.CLIENT.soundfontPath.get());
        this.localSynth = new LocalPlayerMIMISynth(false, ModConfigs.CLIENT.latency.get(), this.soundbank);
        this.mechSynth = new MechanicalMaestroMIMISynth(true, ModConfigs.CLIENT.latency.get(), this.soundbank);
        this.playerSynth = new ServerPlayerMIMISynth(true, ModConfigs.CLIENT.latency.get(), this.soundbank);
    }

    @Override
    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isLocalPlayer()) {
            return;
        }

        midiTickCounter++;

        // Tick local synth every tick
        localSynth.tick(event.player);

        // Tick other synths every N tickets
        if(midiTickCounter >= MIDI_TICK_FREQUENCY) {
    
            // Mechanical Maestros
            mechSynth.tick(event.player);
    
            // Players
            playerSynth.tick(event.player);
        }
    }

    @Override
    @SubscribeEvent
    public void handleSelfLogOut(LoggedOutEvent event) {
        this.close();
    }

    @Override
    public void close() {
        this.allNotesOff();
    }

    @Override
    public void handlePacket(MidiNotePacket message) {
        AMIMISynth<?> targetSynth = getSynthForMessage(message);

        if(targetSynth != null && message.velocity > 0) {
            targetSynth.noteOn(message);
        } else if(targetSynth != null && message.velocity <= 0) {
            targetSynth.noteOff(message);
        }        
    }

    @Override
    public void handleLocalPacket(MidiNotePacket message) {
        LocalPlayerMIMISynth targetSynth = localSynth;

        if(targetSynth != null && message.velocity > 0) {
            targetSynth.noteOn(message);
        } else if(targetSynth != null && message.velocity <= 0) {
            targetSynth.noteOff(message);
        }        
    }

    @Override
    public void allNotesOff() {

        localSynth.allNotesOff();
        mechSynth.allNotesOff();
        playerSynth.allNotesOff();
    }

    protected AMIMISynth<?> getSynthForMessage(MidiNotePacket message) {
        if(message.player.equals(TileMechanicalMaestro.MECH_UUID)) {
            return mechSynth;
        } else {
            return playerSynth;
        }
    }
    
    protected Soundbank openSoundbank(String resourcePath) {
        if(resourcePath != null && !resourcePath.trim().isEmpty()) {
            try {
                return new SF2SoundbankReader().getSoundbank(new BufferedInputStream(new FileInputStream(new File(resourcePath.trim()))));
            } catch(NullPointerException | IOException | InvalidMidiDataException e) {
                MIMIMod.LOGGER.error("Failed to load SoundFont. Error: ", e);
            }
        }

        return null;
    }
}
