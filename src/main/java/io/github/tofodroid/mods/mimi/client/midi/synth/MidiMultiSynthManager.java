package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.github.tofodroid.com.sun.media.sound.SF2SoundbankReader;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Soundbank;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrument;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerTimeSyncPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

public class MidiMultiSynthManager {
    private static final Integer MIDI_TICK_FREQUENCY = 2;
    protected Boolean loggingOff = false;
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
    }

    @SuppressWarnings("resource")
    public void handleClientTick() {
        midiTickCounter++;

        // Tick synths every N tickets
        if(midiTickCounter >= MIDI_TICK_FREQUENCY) {
            if(Minecraft.getInstance().player != null) {
                // Local
                localSynth.tick(Minecraft.getInstance().player);
    
                // Mechanical Maestros
                mechSynth.tick(Minecraft.getInstance().player);
        
                // Players
                playerSynth.tick(Minecraft.getInstance().player);
            }

           
        }
    }

    public void handleLogout() {
        this.close();
        this.loggingOff = true;
    }

    public void handleLogin() {
        this.loggingOff = false;
        this.reloadSynths();
        NetworkManager.INFO_CHANNEL.sendToServer(new ServerTimeSyncPacket());
    }

    public void reloadSynths() {
        if(localSynth != null)
            localSynth.close();
        if(mechSynth != null)
            mechSynth.close();
        if(playerSynth != null)
            playerSynth.close();
        this.mechSynth = new MechanicalMaestroMIMISynth(ModConfigs.CLIENT.jitterCorrection.get(), ModConfigs.CLIENT.latency.get(), this.soundbank);
        this.playerSynth = new ServerPlayerMIMISynth(ModConfigs.CLIENT.jitterCorrection.get(), ModConfigs.CLIENT.latency.get(), this.soundbank);
        this.localSynth = new LocalPlayerMIMISynth(false, ModConfigs.CLIENT.localLatency.get(), this.soundbank);
    }

    public Long getBufferTime(Long noteServerTime) {
        return (MIMIMod.proxy.getBaselineBufferMs() + (Minecraft.getInstance().getCurrentServer() != null ? ModConfigs.CLIENT.noteBufferMs.get() : 0)) + (MIMIMod.proxy.getServerStartEpoch() + noteServerTime);
    }

    public void close() {
        if(localSynth != null) {
            localSynth.allNotesOff();
            localSynth.close();
        }

        if(mechSynth != null) {
            mechSynth.allNotesOff();
            mechSynth.close();
        }

        if(playerSynth != null) {
            playerSynth.allNotesOff();
            playerSynth.close();
        }
    }

    @SuppressWarnings({"resource", "null"})
    public void sendToGui(MidiNotePacket message) {
        if(Minecraft.getInstance().player == null || !message.player.equals(Minecraft.getInstance().player.getUUID()) || Minecraft.getInstance().screen == null || !(Minecraft.getInstance().screen instanceof GuiInstrument)) {
            return;
        }

        GuiInstrument gui = (GuiInstrument)Minecraft.getInstance().screen;

        if(message.instrumentId == gui.getInstrumentId() && message.instrumentHand == gui.getHandIn()) {
            if(message.velocity > 0) {
                gui.onExternalNotePress(message.note);
            } else {
                gui.onExternalNoteRelease(message.note);
            }
        }
    }

    public void handlePacket(MidiNotePacket message) {
        if(loggingOff) return;

        AMIMISynth<?> targetSynth = getSynthForMessage(message);

        if(targetSynth != null) {
            if(!message.isControlPacket()) {
                if(message.velocity > 0) {
                    targetSynth.noteOn(message, getBufferTime(message.noteServerTime));
                } else if(message.velocity <= 0) {
                    targetSynth.noteOff(message, getBufferTime(message.noteServerTime));
                }
                this.sendToGui(message);
            } else if(message.isControlPacket() && !message.isAllNotesOffPacket()) {
                targetSynth.controlChange(message, getBufferTime(message.noteServerTime));
            }
        }
    }
    
    public void handleLocalPacketInstant(MidiNotePacket message) {
        if(loggingOff) return;

        if(localSynth != null) {
            if(!message.isControlPacket()) {
                if(message.velocity > 0) {
                    localSynth.noteOn(message, Util.getEpochMillis());
                } else if(message.velocity <= 0) {
                    localSynth.noteOff(message, Util.getEpochMillis());
                }
            } else if(message.isControlPacket() && !message.isAllNotesOffPacket()) {
                localSynth.controlChange(message, Util.getEpochMillis());
            }
        }
    }

    public void allNotesOff() {
        if(localSynth != null)
            localSynth.allNotesOff();
        if(mechSynth != null)
            mechSynth.allNotesOff();
        if(playerSynth != null)
            playerSynth.allNotesOff();
    }

    protected AMIMISynth<?> getSynthForMessage(MidiNotePacket message) {
        if(message.player.equals(TileMechanicalMaestro.MECH_SOURCE_ID)) {
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
