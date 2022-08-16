package io.github.tofodroid.mods.mimi.common.midi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiStatus.STATUS_CODE;
import io.github.tofodroid.mods.mimi.util.RemoteMidiUrlUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MidiFileCacheManager {
    protected static TreeMap<Instant,String> SEQUENCE_CACHE_ORDER_MAP = new TreeMap<>();
    protected static TreeMap<String,File> SEQUENCE_CACHE_VALUE_MAP = new TreeMap<>();
    protected static TreeMap<String,File> SERVER_SEQUENCE_VALUE_MAP = new TreeMap<>();
    protected static File SEQUENCE_CACHE_FOLDER = null;
    protected static File SERVER_SEQUENCE_FOLDER = null;
    
    public static void init() {
        try {
            // Create folders if not exists
            File mimiFolder = new File(FMLPaths.CONFIGDIR.get().toString(), "mimi");
            if (!mimiFolder.exists() && !mimiFolder.mkdirs() && !mimiFolder.isDirectory()) {
                throw new IOException("Could not create MIMI config directory!");
            }

            File musicCacheFolder = new File(mimiFolder.getAbsolutePath(), "music_cache");
            if (!musicCacheFolder.exists() && !musicCacheFolder.mkdirs() && !musicCacheFolder.isDirectory()) {
                throw new IOException("Could not create MIMI server music cache directory!");
            }
            SEQUENCE_CACHE_FOLDER = musicCacheFolder;
            
            File serverMusicFolder = new File(mimiFolder.getAbsolutePath(), "default_music");
            if (!serverMusicFolder.exists() && !serverMusicFolder.mkdirs() && !serverMusicFolder.isDirectory()) {
                throw new IOException("Could not create MIMI default music directory!");
            }
            SERVER_SEQUENCE_FOLDER = serverMusicFolder;

            refreshSequenceCacheMaps();
            refreshServerSequenceMap();
        } catch(Exception e) {
            throw new IllegalStateException("Failed to configure server music cache.", e);
        }
    }

    public static Integer getCachedFileNamePages(Integer pageSize) {
        return Double.valueOf(Math.ceil((double)SEQUENCE_CACHE_VALUE_MAP.size() / (double)pageSize)).intValue();
    }

    public static Integer getServerFileNamesPages(Integer pageSize) {
        return Double.valueOf(Math.ceil((double)SERVER_SEQUENCE_VALUE_MAP.size() / (double)pageSize)).intValue();
    }

    public static List<String> getCachedFileNames(Integer pageSize, Integer pageNum) {
        List<String> orderedSequenceList = new ArrayList<>(SEQUENCE_CACHE_VALUE_MAP.keySet());

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


    public static void refreshSequenceCacheMaps() {
        SEQUENCE_CACHE_ORDER_MAP = new TreeMap<>();
        SEQUENCE_CACHE_VALUE_MAP = new TreeMap<>();

        if(ModConfigs.COMMON.serverMusicCacheSize.get() > 0) {
            try {
                for(File file : SEQUENCE_CACHE_FOLDER.listFiles()) {
                    if(file.isFile() && file.getAbsolutePath().endsWith(".mid")) {
                        SEQUENCE_CACHE_ORDER_MAP.put(Files.getLastModifiedTime(file.toPath()).toInstant(), file.getName());
                        SEQUENCE_CACHE_VALUE_MAP.put(file.getName(), file);
                    }
                }
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to load existing server music cache: ", e);
            }
        }
    }

    public static void refreshServerSequenceMap() {
        SERVER_SEQUENCE_VALUE_MAP = new TreeMap<>();

        if(ModConfigs.COMMON.serverMusicCacheSize.get() > 0) {
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

    }

    protected static String urlToFile(String url) {
        String file = url
            .replace("https","")
            .replace("http", "")
            .replace("://","")
            .replace(".", "_")
            .replace("/", "_");
        return file + ".mid";
    }

    protected static String urlToServerFile(String url) {
        String file = url
            .replace("server://","")
            .replace("/", "");

        return file;
    }

    public static Pair<Sequence,STATUS_CODE> getOrCreateCachedSequence(String midiUrl) {
        if(midiUrl != null && !midiUrl.isBlank() && midiUrl.toLowerCase().startsWith("server://")) {
            if(!RemoteMidiUrlUtils.validateFileUrl(midiUrl)) {
                return Pair.of(null, STATUS_CODE.ERROR_URL);
            }

            String fileName = urlToServerFile(midiUrl);
    
            if(SERVER_SEQUENCE_VALUE_MAP.containsKey(fileName)) {
                try {
                    Sequence sequence = loadSequence(new File(SERVER_SEQUENCE_FOLDER, fileName));
                    return Pair.of(sequence, null);
                } catch(Exception e) {
                    // Nothing
                }
            }

            return Pair.of(null, STATUS_CODE.ERROR_NOT_FOUND);
        } else if(ModConfigs.COMMON.allowWebMidi.get()) {
            if(midiUrl != null && !midiUrl.isBlank() && RemoteMidiUrlUtils.validateMidiUrl(midiUrl)) {
                if(!RemoteMidiUrlUtils.validateMidiHost(midiUrl)) {
                    return Pair.of(null, STATUS_CODE.ERROR_HOST);
                }

                String fileName = urlToFile(midiUrl);
    
                if(SEQUENCE_CACHE_VALUE_MAP.containsKey(fileName)) {
                    try {
                        Sequence sequence = loadSequence(new File(SEQUENCE_CACHE_FOLDER, fileName));
                        return Pair.of(sequence, null);
                    } catch(Exception e) {
                        MIMIMod.LOGGER.error("Failed to load cached MIDI file: ", e);
                    }
                } else {
                    try {
                        Sequence sequence = downloadSequence(midiUrl);
                        
                        if(ModConfigs.COMMON.serverMusicCacheSize.get() > 0) {
                            pruneSequenceCache();
                            File savedSequence = saveSequence(SEQUENCE_CACHE_FOLDER, fileName, sequence);
                            SEQUENCE_CACHE_VALUE_MAP.put(fileName, savedSequence);
                            SEQUENCE_CACHE_ORDER_MAP.put(Files.getLastModifiedTime(savedSequence.toPath()).toInstant(), fileName);
                        }
    
                        return Pair.of(sequence, null);
                    } catch(FileNotFoundException e) {
                        // 404, Don't handle
                    } catch(MalformedURLException e) {
                        // Bad URL, Don't handle
                    } catch(InvalidMidiDataException e) {
                        // Bad MIDI, Don't handle
                    } catch(Exception e) {
                        MIMIMod.LOGGER.warn("Failed to download and cache MIDI file: ", e);
                    }
                }
            }
            
            return Pair.of(null, STATUS_CODE.ERROR_OTHER);
        }

        return Pair.of(null, STATUS_CODE.ERROR_DISABLED);
    }

    public static STATUS_CODE addServerMusic(String midiUrl) {
        // TODO
        return STATUS_CODE.SUCCESS;
    }
    
    public static void removeServerMusic(String key) {
        // TODO
    }

    protected static Sequence loadSequence(File targetFile) throws IOException, InvalidMidiDataException {
        if(targetFile.exists() && targetFile.isFile()) {
            return MidiSystem.getSequence(targetFile);
        }
        
        throw new IOException("Expected cached MIDI file '" + targetFile.getName() + "' not found.");
    }

    protected static File saveSequence(File folder, String fileName, Sequence sequence) throws IOException {
        File targetFile = new File(folder, fileName);
        try(FileOutputStream fout = new FileOutputStream(targetFile)) {
            MidiSystem.write(sequence, MidiSystem.getMidiFileTypes(sequence)[0], fout);
            fout.close();
            return targetFile;
        } catch(Exception e) {
            throw new IOException(e);
        }        
    }

    protected static Sequence downloadSequence(String midiUrl) throws IOException, InvalidMidiDataException {
        MIMIMod.LOGGER.info("Downloading new MIDI from URL: '" + midiUrl + "'");
        URL url = new URL(midiUrl);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(5000);
        conn.connect(); 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(conn.getInputStream(), baos);
        try(ByteArrayInputStream stream = new ByteArrayInputStream(baos.toByteArray())) {
            return MidiSystem.getSequence(stream);
        } catch(Exception e) {
            throw e;
        }
    }

    public static void pruneSequenceCache() {
        if(!SEQUENCE_CACHE_VALUE_MAP.isEmpty() && (SEQUENCE_CACHE_VALUE_MAP.size() + 1) > ModConfigs.COMMON.serverMusicCacheSize.get()) {
            Integer cycle = 0;
            List<String> toRemove = new ArrayList<>();

            while(
                ((SEQUENCE_CACHE_VALUE_MAP.size() + 1) - toRemove.size()) > ModConfigs.COMMON.serverMusicCacheSize.get() 
                && cycle <= SEQUENCE_CACHE_VALUE_MAP.size()
            ) {
                cycle++;
                toRemove.add(SEQUENCE_CACHE_ORDER_MAP.pollFirstEntry().getValue());
            }
            
            for(String removeKey : toRemove) {
                MIMIMod.LOGGER.info("Pruning cached song: '" + removeKey + "'");
                SEQUENCE_CACHE_VALUE_MAP.remove(removeKey);
                try {
                    Files.deleteIfExists(new File(SEQUENCE_CACHE_FOLDER, removeKey).toPath());
                } catch(IOException e) {
                    MIMIMod.LOGGER.error("Failed to prune cached song: " + removeKey);
                }
            }
        }
    }

    public static void emptySequenceCache() {
        if(!SEQUENCE_CACHE_VALUE_MAP.isEmpty()) {
            List<String> toRemove = new ArrayList<>(SEQUENCE_CACHE_VALUE_MAP.keySet());
            
            for(String removeKey : toRemove) {
                MIMIMod.LOGGER.info("Deleting cached song: '" + removeKey + "'");
                try {
                    Files.deleteIfExists(new File(SEQUENCE_CACHE_FOLDER, removeKey).toPath());
                } catch(IOException e) {
                    MIMIMod.LOGGER.error("Failed to prune cached song: " + removeKey);
                }
            }

            SEQUENCE_CACHE_ORDER_MAP = new TreeMap<>();
            SEQUENCE_CACHE_VALUE_MAP = new TreeMap<>();
        }
    }
}
