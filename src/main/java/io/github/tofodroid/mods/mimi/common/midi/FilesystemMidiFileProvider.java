package io.github.tofodroid.mods.mimi.common.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FilesystemMidiFileProvider implements IMidiFileProvider {
    public static final String MIMI_CONFIG_DIR = "mimi";
    public static final String CLIENT_MIDI_DIR = "midi_files";
    public static final String SERVER_MIDI_DIR = "server_midi_files";

    protected final Boolean isServer;
    protected Map<UUID, LocalMidiInfo> songMap;
    protected List<UUID> orderedSongList;
    protected File selectedFolder = null;

    public FilesystemMidiFileProvider(Boolean isServer) {
        this.isServer = isServer;
        this.initFromConfigFolder();
    }

    @Override
    public void clear() {
        this.songMap = new HashMap<>();
        this.orderedSongList = new ArrayList<>();
    }
    
    @Override
    public List<UUID> getSortedSongIds() {
        return this.orderedSongList;
    }

    @Override
    public Map<UUID, LocalMidiInfo> getSongMap() {
        return this.songMap;
    }

    @Override
    public void loadSongs() {
        this.clear();
        List<LocalMidiInfo> allFiles = loadFilesystemSongs();
        this.orderedSongList = this.buildOrderList(allFiles);
        this.songMap = this.buildSongMap(allFiles);
    }

    public String getCurrentFolderPath() {
        return selectedFolder.getAbsolutePath();
    }

    protected void initFromDirectory(File directory) {
        this.clear();

        if(directory != null && directory.exists() && directory.isDirectory()) {
            selectedFolder = directory;
            this.loadSongs();
        }
    }

    public FilesystemMidiFileProvider initFromConfigFolder() {
        try {
            File mimiFolder = new File(FMLPaths.CONFIGDIR.get().toString(), MIMI_CONFIG_DIR);
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

    protected List<LocalMidiInfo> loadFilesystemSongs() {
        List<LocalMidiInfo> allFiles = new ArrayList<>();
        try {
            for(File file : selectedFolder.listFiles()) {
                if(file.isFile() && (file.getAbsolutePath().endsWith(".mid") || file.getAbsolutePath().endsWith(".midi"))) {
                    LocalMidiInfo info = LocalMidiInfo.fromFile(file);
                    if(info != null) {
                        allFiles.add(info);
                    }
                }
            }
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to load MIDI files from directory '" + selectedFolder.getAbsolutePath() + "'. ", e);
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
