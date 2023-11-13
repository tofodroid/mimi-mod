package io.github.tofodroid.mods.mimi.util;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import io.github.tofodroid.mods.mimi.common.midi.DrumKitName;
import io.github.tofodroid.mods.mimi.common.midi.MidiPatchName;

public abstract class MidiFileUtils {
    public static byte[] getChannelMapping(Sequence sequence) {
        // Initialize
        byte[] result = new byte[16];
        for(int i = 0; i < 16; i++) {
            result[i] = -1;
        }

        // Map
        for(Track track : sequence.getTracks()) {
            if(track != null && track.size() > 0) {
                for(int i = 0; i < track.size(); i++) {
                    if(track.get(i).getMessage() instanceof ShortMessage) {
                        ShortMessage message = (ShortMessage)track.get(i).getMessage();
                        if(message.getChannel() != 9 && ShortMessage.PROGRAM_CHANGE == message.getCommand() && result[message.getChannel()] == -1) {
                            result[message.getChannel()] = Integer.valueOf(message.getData1()).byteValue();
                        } else if(message.getChannel() == 9 && ShortMessage.PROGRAM_CHANGE == message.getCommand()) {
                            result[message.getChannel()] = Integer.valueOf(message.getData1()).byteValue();
                        } else if(message.getChannel() == 9 && ShortMessage.NOTE_ON == message.getCommand()) {
                            result[message.getChannel()] = 0;
                        }
                    }
                }
            }
        }

        return result;
    }

    public static Map<Integer, String> getInstrumentMapping(byte[] channelMap) {
        Map<Integer, String> mapping = new HashMap<>();

        // Initialize
        for(int i = 0; i < 16; i++) {
            mapping.put(i, null);
        }

        for(int i = 0; i < 16; i++) {
            if(channelMap[i] >= 0) {
                if(i != 9) {
                    mapping.put(i, MidiPatchName.getForPatch(Byte.valueOf(channelMap[i]).intValue()).name);
                } else {
                    mapping.put(i, DrumKitName.getForPatch(Byte.valueOf(channelMap[i]).intValue()).name);
                }
            }
        }

        return mapping;
    }

    public static Integer getSongLenghtSeconds(Sequence sequence) {
        return Integer.parseInt(Long.valueOf(sequence.getMicrosecondLength()/1000000).toString());
    }

    public static Integer getTempoBPM(Sequence sequence) {
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
