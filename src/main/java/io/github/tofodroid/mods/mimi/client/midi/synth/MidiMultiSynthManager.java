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
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class MidiMultiSynthManager {
    private static final Integer MIDI_TICK_FREQUENCY = 4;

    protected Soundbank soundbank = null;
    protected Integer midiTickCounter = 0;
    protected LocalPlayerMIMISynth localSynth;
    protected MechanicalMaestroMIMISynth mechSynth;
    protected ServerPlayerMIMISynth playerSynth;

    public MidiMultiSynthManager() {
        this.soundbank = openSoundbank(ModConfigs.CLIENT.soundfontPath.get());

        if(this.soundbank != null) {
            MIMIMod.LOGGER.debug("Loaded Soundbank:\n\n" +
                "\tName: " + this.soundbank.getName() + "\n" +
                "\tDesc: " + this.soundbank.getDescription() + "\n" +
                "\tVers: " + this.soundbank.getVersion() + "\n" +
                "\tVend: " + this.soundbank.getVendor() + "\n"
            );
        }

        this.mechSynth = new MechanicalMaestroMIMISynth(ModConfigs.CLIENT.jitterCorrection.get(), ModConfigs.CLIENT.latency.get(), this.soundbank);
        this.playerSynth = new ServerPlayerMIMISynth(ModConfigs.CLIENT.jitterCorrection.get(), ModConfigs.CLIENT.latency.get(), this.soundbank);
        this.localSynth = new LocalPlayerMIMISynth(false, ModConfigs.CLIENT.localLatency.get(), this.soundbank);
    }

    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isLocalPlayer()) {
            return;
        }

        midiTickCounter++;

        // Tick synths every N tickets
        if(midiTickCounter >= MIDI_TICK_FREQUENCY) {
            // Local
            localSynth.tick(event.player);
    
            // Mechanical Maestros
            mechSynth.tick(event.player);
    
            // Players
            playerSynth.tick(event.player);
        }
    }

    @SubscribeEvent
    public void handleSelfLogOut(LoggingOut event) {
        this.close();
    }

    public void close() {
        this.allNotesOff();
    }

    public void handlePacket(MidiNotePacket message) {
        AMIMISynth<?> targetSynth = getSynthForMessage(message);

        if(targetSynth != null) {
            if(!message.isControlPacket()) {
                if(message.velocity > 0) {
                    targetSynth.noteOn(message);
                } else if(message.velocity <= 0) {
                    targetSynth.noteOff(message);
                }
            } else {
                targetSynth.controlChange(message);
            }
        }
    }
    
    public void handleLocalPacket(MidiNotePacket message) {
        if(localSynth != null) {
            if(!message.isControlPacket()) {
                if(message.velocity > 0) {
                    localSynth.noteOn(message);
                } else if(message.velocity <= 0) {
                    localSynth.noteOff(message);
                }
            } else {
                localSynth.controlChange(message);
            }
        }
    }

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
                MIMIMod.LOGGER.warn("Failed to load user SoundFont. Error: ", e);
            }
        }
        
        try {
            return new SF2SoundbankReader().getSoundbank(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("assets/mimi/soundfont/GMGSX.SF2")));
        } catch(NullPointerException | IOException | InvalidMidiDataException e) {
            MIMIMod.LOGGER.error("Failed to load MIMI SoundFont. Error: ", e);
        }

        return null;
    }
}
