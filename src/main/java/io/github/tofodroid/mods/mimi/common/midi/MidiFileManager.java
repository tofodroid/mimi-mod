package io.github.tofodroid.mods.mimi.common.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MidiFileManager {
    public static final String MIMI_CONFIG_DIR = "mimi";
    public static final String MIMI_MIDI_DIR = "midi_files";

    protected TreeMap<UUID, MidiFileInfo> songMap = new TreeMap<>();
    protected File selectedFolder = null;

    public MidiFileManager() {}

    public void clear() {
        this.songMap = new TreeMap<>();
    }

    public void refresh() {
        this.initFromDirectory(selectedFolder);
    }

    public String getCurrentFolderPath() {
        return selectedFolder.getAbsolutePath();
    }

    protected void initFromDirectory(File directory) {
        this.clear();

        if(directory != null && directory.exists() && directory.isDirectory()) {
            selectedFolder = directory;
            loadSongMap();
        }
    }
    
    public void initFromAbsolutePath(String folderPath) {
        if(folderPath != null && !folderPath.trim().isEmpty() && Files.isDirectory(Paths.get(folderPath.trim()), LinkOption.NOFOLLOW_LINKS)) {
            this.initFromDirectory(new File(folderPath));
        } else {
            this.clear();
        }
    }

    public void initFromConfigFolder() {
        try {
            File mimiFolder = new File(FMLPaths.CONFIGDIR.get().toString(), MIMI_CONFIG_DIR);
            if (!mimiFolder.exists() && !mimiFolder.mkdirs() && !mimiFolder.isDirectory()) {
                throw new IOException("Could not create MIMI config directory!");
            }
            
            File midiFolder = new File(mimiFolder.getAbsolutePath(), MIMI_MIDI_DIR);
            if (!midiFolder.exists() && !midiFolder.mkdirs() && !midiFolder.isDirectory()) {
                throw new IOException("Could not create MIMI MIDI directory!");
            }

            this.initFromDirectory(midiFolder);
        } catch(Exception e) {
            this.clear();
            MIMIMod.LOGGER.error("Failed to configure MIDI file manager.", e);
        }
    }

    public Integer getFileInfoPages(Integer pageSize) {
        return Double.valueOf(Math.ceil((double)songMap.size() / (double)pageSize)).intValue();
    }

        public List<MidiFileInfo> getFileInfoPage(Integer pageSize, Integer pageNum) {
        List<MidiFileInfo> pageList = new ArrayList<>(songMap.values());

        if(pageList.size() <= pageSize) {
            return pageList;
        } else {
            List<MidiFileInfo> result = new ArrayList<>();
            Integer startingIndex = pageSize * pageNum;
            for(int i = startingIndex; i < (startingIndex + pageSize); i++) {
                if(i >= pageList.size()) {
                    return result;
                }
                result.add(pageList.get(i));
            }
            return result;
        }
    }


    public List<MidiFileInfo> getAllSongs() {
        return new ArrayList<>(songMap.values());
    }
    

    public void loadSongMap() {
        songMap = new TreeMap<>();
        try {
            for(File file : selectedFolder.listFiles()) {
                if(file.isFile() && (file.getAbsolutePath().endsWith(".mid") || file.getAbsolutePath().endsWith(".midi"))) {
                    MidiFileInfo info = MidiFileInfo.fromFile(file);
                    if(info != null) {
                        songMap.put(info.toUUID(), info);
                    }
                }
            }
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to load MIDI files from directory '" + selectedFolder.getAbsolutePath() + "'. ", e);
        }
    }
    
    public Sequence getSequenceById(UUID id) {
        return songMap.get(id).getSequence();
    }

    public Sequence getSequenceByIndex(Integer index) {
        try {
            return getAllSongs().get(index).getSequence();
        } catch(Exception e) {
            return null;
        }
    }

    public File saveSequenceToCurrentFolder(String fileName, Sequence sequence, Boolean overwrite) throws IOException {
        if(selectedFolder != null) {
            File targetFile = new File(selectedFolder, fileName.replace(" ", "_").replaceAll("[^A-Za-z0-9_-]", "_") + ".mid");

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

        return null;
    }
}
