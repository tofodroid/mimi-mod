package io.github.tofodroid.mods.mimi.common.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;

public class FilesystemMidiFileProvider {
    public static final String MIMI_CONFIG_DIR = "mimi";
    public static final String CLIENT_MIDI_DIR = "midi_files";
    public static final String SERVER_MIDI_DIR = "server_midi_files";
    public static final FilenameFilter MIDI_FILTER = (dir, name) -> name.endsWith(".mid") || name.endsWith(".midi");
    protected final Integer updateAfterSeconds;
    protected final Boolean isServer;
    protected Map<UUID, LocalMidiInfo> songMap;
    protected List<UUID> orderedSongList;
    protected Instant lastLoad = Instant.MIN;
    protected File selectedFolder = null;
    protected String lastFolderHash = null;

    public FilesystemMidiFileProvider(Boolean isServer, Integer updateAfterSeconds) {
        this.isServer = isServer;
        this.updateAfterSeconds = updateAfterSeconds;
        this.songMap = new HashMap<>();
        this.orderedSongList = new ArrayList<>();
        this.initFromConfigFolder();
    }

    protected void clear() {
        this.songMap = new HashMap<>();
        this.orderedSongList = new ArrayList<>();
    }
    
    public List<UUID> getSortedSongIds() {
        return this.orderedSongList;
    }
    
    public void refresh(Boolean forceFromDisk) {
        this.loadSongs(forceFromDisk);
    }

    public List<BasicMidiInfo> getSortedSongInfos() {
        List<BasicMidiInfo> result = new ArrayList<>();
        for(UUID id : this.getSortedSongIds()) {
            result.add(this.getInfoById(id).getBasicMidiInfo());
        }
        return result;
    }

    public LocalMidiInfo getInfoById(UUID id) {
        return this.songMap.get(id);
    }

    public Integer getSongCount() {
        return this.songMap.size();
    }

    public Boolean isEmpty() {
        return this.songMap.isEmpty();
    }

    public HashMap<UUID, LocalMidiInfo> buildSongMap(List<LocalMidiInfo> allFiles) {
        HashMap<UUID, LocalMidiInfo> resultMap = new HashMap<>();
        allFiles.forEach(midiFile -> {
            resultMap.put(midiFile.fileId, midiFile);
        });
        return resultMap;
    }
    
    public List<UUID> buildOrderList(List<LocalMidiInfo> allFiles) {
        List<LocalMidiInfo> copy = new ArrayList<LocalMidiInfo>(allFiles);
        copy.sort((midiA, midiB) -> {
            return midiA.file.getName().toLowerCase().trim().compareTo(midiB.file.getName().toLowerCase().trim());
        });
        List<UUID> result = new ArrayList<>();

        for(LocalMidiInfo info : copy) {
            UUID songId = info.fileId;

            if(!result.contains(songId)) {
                result.add(songId);
            }
        }
        return result;
    }

    public void loadSongs(Boolean forceFromDisk) {
        if(!forceFromDisk && Instant.now().isBefore(this.lastLoad.plusSeconds(this.updateAfterSeconds))) {
            return;
        }

        this.lastLoad = Instant.now();

        if(this.folderExists()) {
            File[] midiFiles = this.folderHasChanges(forceFromDisk);

            if(midiFiles != null) {
                this.clear();
                List<LocalMidiInfo> allFiles = loadFilesystemSongs(midiFiles);
                this.songMap = this.buildSongMap(allFiles);
                this.orderedSongList = this.buildOrderList(allFiles);
            }
        } else {
            this.clear();
            this.lastFolderHash = null;
            MIMIMod.LOGGER.error("MIMI MIDI Folder no longer exists: " + this.selectedFolder.getAbsolutePath());
        }
    }

    public Boolean folderExists() {
        return this.selectedFolder.exists() && this.selectedFolder.isDirectory();
    }

    public File[] folderHasChanges(Boolean forceFromDisk) {
        File[] files = this.selectedFolder.listFiles(MIDI_FILTER);

        Long totalSize = 0l;
        Long totalModified = 0l;
        Long totalNameHash = 0l;
        Long totalNameSize = 0l;

        for(File file : files) {
            totalSize += file.length();
            totalModified += file.lastModified();
            totalNameHash += file.getName().hashCode();
            totalNameSize += file.getName().length();
        }

        String newFolderHash = "folder-"+totalNameSize+"-"+totalSize+"-"+totalModified+"-"+totalNameHash;

        if(!newFolderHash.equals(this.lastFolderHash) || forceFromDisk) {
            this.lastFolderHash = newFolderHash;
            return files;
        }

        return null;
    }

    public String getCurrentFolderPath() {
        return selectedFolder.getAbsolutePath();
    }

    protected void initFromDirectory(File directory) {
        this.clear();

        if(directory != null && directory.exists() && directory.isDirectory()) {
            selectedFolder = directory;
            this.loadSongs(true);
        }
    }

    public FilesystemMidiFileProvider initFromConfigFolder() {
        try {
            File mimiFolder = new File(ConfigProxy.getConfigPath().toString(), MIMI_CONFIG_DIR);
            if (!mimiFolder.exists() && !mimiFolder.mkdirs() && !mimiFolder.isDirectory()) {
                throw new IOException("Could not create MIMI config directory!");
            }
            
            File midiFolder = new File(mimiFolder.getAbsolutePath(), isServer ? SERVER_MIDI_DIR : CLIENT_MIDI_DIR);
            if (!midiFolder.exists() && !midiFolder.mkdirs() && !midiFolder.isDirectory()) {
                throw new IOException("Could not create MIMI MIDI directory!");
            }

            this.initFromDirectory(midiFolder);
        } catch(Exception e) {
            this.clear();
            MIMIMod.LOGGER.error("Failed to configure MIDI file manager.", e);
        }
        return this;
    }

    protected List<LocalMidiInfo> loadFilesystemSongs(File[] midiFiles) {
        List<LocalMidiInfo> allFiles = new ArrayList<>();
        try {
            for(File file : midiFiles) {
                LocalMidiInfo info = LocalMidiInfo.fromFile(file);
                if(info != null) {
                    allFiles.add(info);
                }
            }
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to load MIDI files from directory '" + selectedFolder.getAbsolutePath() + "' - ", e.getMessage());
        }
        return allFiles;
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
