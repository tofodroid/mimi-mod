package io.github.tofodroid.mods.mimi.client.midi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import io.github.tofodroid.mods.mimi.common.midi.DrumKitName;
import io.github.tofodroid.mods.mimi.common.midi.MidiPatchName;

public class MidiFileInfo {
    public String fileName;
    public String songLength;
    public Integer tempo;
    public Map<Integer, String> instrumentMapping;

    public static MidiFileInfo fromFile(File file) {
        if(file.exists()) {
            try {
                Sequence sequence = MidiSystem.getSequence(file);
                MidiFileInfo result = new MidiFileInfo();
                result.instrumentMapping = getInstrumentMapping(sequence);
                result.tempo = getTempoBPM(sequence);
                result.fileName = file.getName();
                result.songLength = new Long(TimeUnit.MICROSECONDS.convert(sequence.getMicrosecondLength(), TimeUnit.SECONDS)).toString();
                return result;
            } catch(Exception e) {
                return null;
            }
        }
        return null;
    }

    private static Map<Integer, String> getInstrumentMapping(Sequence sequence) {
        Map<Integer, String> mapping = new HashMap<>();

        // Initialize
        for(int i = 0; i < 16; i++) {
            mapping.put(i, null);
        }

        for(Track track : sequence.getTracks()) {
            if(track != null && track.size() > 0) {
                for(int i = 0; i < track.size(); i++) {
                    if(track.get(i).getMessage() instanceof ShortMessage) {
                        ShortMessage message = (ShortMessage)track.get(i).getMessage();
                        if(message.getChannel() != 9 && ShortMessage.PROGRAM_CHANGE == message.getCommand() && mapping.get(message.getChannel()) == null) {
                            mapping.put(message.getChannel(), MidiPatchName.getForPatch(message.getData1()).name);
                        } else if(message.getChannel() == 9 && ShortMessage.PROGRAM_CHANGE == message.getCommand()) {
                            mapping.put(message.getChannel(), DrumKitName.getForPatch(message.getData1()).name);
                        } else if(message.getChannel() == 9 && ShortMessage.NOTE_ON == message.getCommand()) {
                            mapping.put(message.getChannel(), DrumKitName.getForPatch(0).name);
                        }
                    }
                }
            }
        }

        return mapping;
    }

    private static Integer getTempoBPM(Sequence sequence) {
        for(Track track : sequence.getTracks()) {
            if(track != null && track.size() > 0) {
                for(int i = 0; i < track.size(); i++) {
                    if(track.get(i).getMessage() instanceof MetaMessage) {
                        MetaMessage message = (MetaMessage)track.get(i).getMessage();
                        if(message.getType() == 81 && message.getData().length == 3) {
                            byte[] data = message.getData();
                            int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                            return Math.round(60000001f / mspq);
                        }
                    }
                }
            }
        }

        return 120;        
    }
}
