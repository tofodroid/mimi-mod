package io.github.tofodroid.mods.mimi.client.midi;

import java.io.File;
import java.net.URL;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

public class MidiInputSequenceManager extends MidiInputSourceManager {
    private Sequencer activeSequencer = null;
    private Sequence activeSequence = null;
    private String activeSequenceSource = null;

    public MidiInputSequenceManager() {
        try {
            this.activeSequencer = MidiSystem.getSequencer(false);
            this.activeSequencer.open();
        } catch(Exception e) {
            // TODO
        }
    }

    public Boolean sequencerAvailable() {
        return this.activeSequencer != null && this.activeSequencer.isOpen();
    }

    public Sequencer getSequencer() {
        return this.activeSequencer;
    }

    public Boolean alreadyRunningSequence(String source) {
        return source != null && this.activeSequenceSource != null && source.equalsIgnoreCase(this.activeSequenceSource);
    }

    public Boolean loadSequenceFromUrl(String url) {
        if(sequencerAvailable()) {
            try {
                activeSequence = MidiSystem.getSequence(new URL(url));
                activeSequenceSource = url;
                activeSequencer.setSequence(activeSequence);
                return true;
            } catch(Exception e) {
                return false;
            }
        }

        return false;
    }

    public Boolean loadSequenceFromFile(String filePath) {
        if(sequencerAvailable()) {
            try {
                activeSequence = MidiSystem.getSequence(new File(filePath));
                activeSequenceSource = filePath;
                activeSequencer.setSequence(activeSequence);
                return true;
            } catch(Exception e) {
                return false;
            }
        }

        return false;
    }

    public void stop() {
        if(this.activeSequencer != null && this.activeSequencer.isRunning()) {
            this.activeSequencer.stop();
            this.activeSequencer.setTickPosition(0);
        }
    }

    public void play() {
        if(this.activeSequencer != null && this.activeSequence != null && this.activeSequencer.isOpen()) {
            this.activeSequencer.start();
        }
    }

    public Boolean isPlaying() {
        return this.activeSequencer != null && this.activeSequencer.isRunning();
    }

    @Override
    protected void openTransmitter() {
        if(this.activeSequencer != null) {
            try {
                this.activeTransmitter = this.activeSequencer.getTransmitter();
                this.activeTransmitter.setReceiver(new MidiSequenceInputReceiver());
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Midi Device Error: ", e);
                close();
            }
        }
    }

    public void open() {
        if(this.activeSequencer == null) {
            try {
                this.activeSequencer = MidiSystem.getSequencer(false);
                this.activeSequencer.open();
            } catch(Exception e) {
                // TODO
            }
        }
        
        if(this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }

    @Override
    public void close() {
        this.stop();
        super.close();

        if(this.activeSequencer != null) {
            this.activeSequencer.close();
            this.activeSequencer = null;
        }
    }
}
