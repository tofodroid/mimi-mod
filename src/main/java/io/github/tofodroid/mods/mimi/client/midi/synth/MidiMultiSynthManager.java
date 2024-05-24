package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.github.tofodroid.com.sun.media.sound.SF2SoundbankReader;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Soundbank;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrument;
import io.github.tofodroid.mods.mimi.client.midi.AudioOutputDeviceManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.ServerTimeSyncPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.util.TimeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MidiMultiSynthManager {
    private static final Integer MIDI_TICK_FREQUENCY = 2;
    protected Boolean loggingOff = false;
    protected Boolean paused = false;
    protected Boolean dead = false;
    protected Soundbank soundbank = null;
    protected Integer midiTickCounter = 0;
    public AudioOutputDeviceManager audioDeviceManager;
    protected LocalPlayerMIMISynth localSynth;
    protected ServerPlayerMIMISynth networkSynth;

    public MidiMultiSynthManager() {
        this.audioDeviceManager = new AudioOutputDeviceManager();
        this.soundbank = openSoundbank(ConfigProxy.getSoundfontPath());

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

        // Pause Synths on Game Paused
        Boolean gamePaused = Minecraft.getInstance().isPaused();
        if(!paused && gamePaused) {
            this.allNotesOff();
            paused = true;
        } else if(paused && !gamePaused) {
            paused = false;
        }

        // Tick synths every N tickets
        if(midiTickCounter >= MIDI_TICK_FREQUENCY) {
            if(Minecraft.getInstance().player != null) {
                // Local
                localSynth.tick(Minecraft.getInstance().player);
            
                // Players
                networkSynth.tick(Minecraft.getInstance().player);
            }
        }
    }

    public void handleLogout() {
        this.close();
        this.loggingOff = true;
        this.dead = true;
    }

    public void handleLogin() {
        this.loggingOff = false;
        this.dead = false;
        this.reloadSynths();
        NetworkProxy.sendToServer(new ServerTimeSyncPacket());
    }

    public void reloadSynths() {
        if(localSynth != null)
            localSynth.close();
        if(networkSynth != null)
            networkSynth.close();

        Pair<AudioFormat, SourceDataLine> netOutLine = audioDeviceManager.getOutputFormatLine();
        this.networkSynth = new ServerPlayerMIMISynth(netOutLine.getLeft(), netOutLine.getRight(), ConfigProxy.getJitterCorrection(), ConfigProxy.getLatency(), this.soundbank);
        
        Pair<AudioFormat, SourceDataLine> localOutLine = audioDeviceManager.getOutputFormatLine();
        this.localSynth = new LocalPlayerMIMISynth(localOutLine.getLeft(), localOutLine.getRight(), ConfigProxy.getJitterCorrection(), ConfigProxy.getLocalLatency(), this.soundbank);
    }

    public Long getBufferTime(Long noteServerTime) {
        return (MIMIMod.getProxy().getBaselineBufferMs() + (Minecraft.getInstance().getCurrentServer() != null ? ConfigProxy.getLocalBufferms() : 0)) + (MIMIMod.getProxy().getServerStartEpoch() + noteServerTime);
    }

    public void close() {
        if(localSynth != null) {
            localSynth.allNotesOff();
            localSynth.close();
        }

        if(networkSynth != null) {
            networkSynth.allNotesOff();
            networkSynth.close();
        }
    }

    @SuppressWarnings("resource")
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

    @SuppressWarnings("resource")
    public void handlePacket(MidiNotePacket message) {
        if(loggingOff || Minecraft.getInstance().player == null)
            return;

        if(networkSynth != null) {
            if(!message.isControlPacket()) {
                if(message.velocity > 0 && !Minecraft.getInstance().isPaused()) {
                    //MIMIMod.LOGGER.info("Note On: " + message.note + " | " + message.instrumentId + " | " + message.noteServerTime);
                    networkSynth.noteOn(message, getBufferTime(message.noteServerTime));
                } else if(message.velocity <= 0) {
                    //MIMIMod.LOGGER.info("Note Off: " + message.note + " | " + message.instrumentId + " | " + message.noteServerTime);
                    networkSynth.noteOff(message, getBufferTime(message.noteServerTime));
                }
                this.sendToGui(message);
            } else if(message.isControlPacket() && !message.isAllNotesOffPacket() && !Minecraft.getInstance().isPaused()) {
                networkSynth.controlChange(message, getBufferTime(message.noteServerTime));
            }
        }
    }
    
    public void handleLocalPacketInstant(MidiNotePacket message) {
        if(loggingOff) return;

        if(localSynth != null) {
            if(!message.isControlPacket()) {
                if(message.velocity > 0) {
                    localSynth.noteOn(message, TimeUtils.getNowTime());
                } else if(message.velocity <= 0) {
                    localSynth.noteOff(message, TimeUtils.getNowTime());
                }
                this.sendToGui(message);
            } else if(message.isControlPacket() && !message.isAllNotesOffPacket()) {
                localSynth.controlChange(message, TimeUtils.getNowTime());
            }
        }
    }

    @SuppressWarnings("resource")
    public void handlePlayerTick(Player player) {
        if(player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            if(!player.isAlive() && !this.dead) {
                this.allNotesOff();
            } else {
                this.dead = !player.isAlive();
            }
        }
    }

    public void allNotesOff() {
        if(localSynth != null)
            localSynth.allNotesOff();
        if(networkSynth != null)
            networkSynth.allNotesOff();
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
