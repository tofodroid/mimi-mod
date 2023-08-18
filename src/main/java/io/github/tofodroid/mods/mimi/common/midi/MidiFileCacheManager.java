package io.github.tofodroid.mods.mimi.common.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MidiFileCacheManager {
    protected static TreeMap<String,File> SERVER_SEQUENCE_VALUE_MAP = new TreeMap<>();
    protected static File SERVER_SEQUENCE_FOLDER = null;
    
    public static void init() {
        try {
            // Create folders if not exists
            File mimiFolder = new File(FMLPaths.CONFIGDIR.get().toString(), "mimi");
            if (!mimiFolder.exists() && !mimiFolder.mkdirs() && !mimiFolder.isDirectory()) {
                throw new IOException("Could not create MIMI config directory!");
            }
            
            File serverMusicFolder = new File(mimiFolder.getAbsolutePath(), "server_midi_files");
            if (!serverMusicFolder.exists() && !serverMusicFolder.mkdirs() && !serverMusicFolder.isDirectory()) {
                throw new IOException("Could not create MIMI default music directory!");
            }
            SERVER_SEQUENCE_FOLDER = serverMusicFolder;

            refreshServerSequenceMap();
        } catch(Exception e) {
            throw new IllegalStateException("Failed to configure server music cache.", e);
        }
    }

    public static Boolean hasServerFile(String key) {
        return SERVER_SEQUENCE_VALUE_MAP.containsKey(key);
    }

    public static Integer getServerFileNamesPages(Integer pageSize) {
        return Double.valueOf(Math.ceil((double)SERVER_SEQUENCE_VALUE_MAP.size() / (double)pageSize)).intValue();
    }
    
    public static List<String> getServerFileNames(Integer pageSize, Integer pageNum) {
        List<String> orderedSequenceList = new ArrayList<>(SERVER_SEQUENCE_VALUE_MAP.keySet());

        if(orderedSequenceList.size() <= pageSize) {
            return orderedSequenceList;
        } else {
            List<String> result = new ArrayList<>();
            Integer startingIndex = pageSize * pageNum;
            for(int i = startingIndex; i < (startingIndex + pageSize); i++) {
                if(i >= orderedSequenceList.size()) {
                    return result;
                }
                result.add(orderedSequenceList.get(i));
            }
            return result;
        }
    }

    public static void refreshServerSequenceMap() {
        SERVER_SEQUENCE_VALUE_MAP = new TreeMap<>();
        try {
            for(File file : SERVER_SEQUENCE_FOLDER.listFiles()) {
                if(file.isFile() && (file.getAbsolutePath().endsWith(".mid") || file.getAbsolutePath().endsWith(".midi"))) {
                    SERVER_SEQUENCE_VALUE_MAP.put(file.getName().substring(0,
                        file.getName().lastIndexOf(".midi") >= 0 ? file.getName().lastIndexOf(".midi") : file.getName().lastIndexOf(".mid")
                    ), file);
                }
            }
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to load existing server default music: ", e);
        }
    }
    
    public static Boolean removeServerMusic(String name) {
        if(SERVER_SEQUENCE_VALUE_MAP.get(name) != null) {
            try {
                Files.deleteIfExists(SERVER_SEQUENCE_VALUE_MAP.get(name).toPath());
                SERVER_SEQUENCE_VALUE_MAP.remove(name);
                return true;
            } catch(IOException e) {
                MIMIMod.LOGGER.error("Failed to delete server song: " + name + ".mid");
            }
        }
        return false;
    }

    public static Sequence getSequenceByIndex(Integer index) {
        try {
            File file = new ArrayList<>(SERVER_SEQUENCE_VALUE_MAP.entrySet()).get(index).getValue();
            return loadSequence(file);
        } catch(Exception e) {
            return null;
        }
    }

    protected static Sequence loadSequence(File targetFile) throws IOException, InvalidMidiDataException {
        if(targetFile.exists() && targetFile.isFile()) {
            return MidiSystem.getSequence(targetFile);
        }
        
        throw new IOException("Expected MIDI file '" + targetFile.getName() + "' not found.");
    }

    public static File saveSequence(String fileName, Sequence sequence, Boolean overwrite) throws IOException {
        File targetFile = new File(SERVER_SEQUENCE_FOLDER, fileName.replace(" ", "_").replaceAll("[^A-Za-z0-9_-]", "_") + ".mid");

        if(targetFile.exists() && !overwrite) {
            throw new IOException("Target file '" + targetFile.getAbsolutePath() + "' already exists and overwrite is disabled!");
        }

        Files.deleteIfExists(targetFile.toPath());

        try(FileOutputStream fout = new FileOutputStream(targetFile)) {
            MidiSystem.write(sequence, MidiSystem.getMidiFileTypes(sequence)[0], fout);
            fout.close();
            return targetFile;
        } catch(Exception e) {
            throw new IOException(e);
        }        
    }
}
