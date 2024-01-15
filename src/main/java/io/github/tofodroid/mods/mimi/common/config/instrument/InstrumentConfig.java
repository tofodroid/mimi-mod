package io.github.tofodroid.mods.mimi.common.config.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import net.minecraftforge.fml.loading.FMLPaths;

public abstract class InstrumentConfig {
    private static List<InstrumentSpec> instrumentSpecs = null;

    public static List<InstrumentSpec> getItemInstruments() {
        return getAllInstruments().stream().filter(s -> !s.isBlock).collect(Collectors.toList());
    }

    public static List<InstrumentSpec> getItemInstruments(Boolean colorable) {
        return getAllInstruments().stream().filter(s -> !s.isBlock && s.isColorable() == colorable).collect(Collectors.toList());
    }

    public static List<InstrumentSpec> getBlockInstruments() {
        return getAllInstruments().stream().filter(s -> s.isBlock).collect(Collectors.toList());
    }

    public static List<InstrumentSpec> getBlockInstruments(Boolean colorable) {
        return getAllInstruments().stream().filter(s -> s.isBlock && s.isColorable() == colorable).collect(Collectors.toList());
    }
    
    public static List<InstrumentSpec> getAllInstruments() {
        return instrumentSpecs;
    }

    private static InputStreamReader getCustomInstrumentJsonStream(Path gameDir) throws IOException {
        // Create folder if not exists
        File mimiFolder = new File(gameDir.toString(), "mimi");
        if (!mimiFolder.exists() && !mimiFolder.mkdirs()) {
            throw new IOException("Could not create MIMI config directory!");
        }

        // Create file if not exists
        File mimiJson = new File(gameDir.toString(), "mimi/custom.json");
        if(!mimiJson.exists()) {
            InputStream defaultData = InstrumentConfig.class.getClassLoader().getResourceAsStream("data/mimi/instruments/custom.json");
            Files.copy(
                defaultData, 
                mimiJson.toPath(), 
                StandardCopyOption.REPLACE_EXISTING
            );
            IOUtils.closeQuietly(defaultData);
        }

        // Load file
        return new InputStreamReader(new FileInputStream(mimiJson));
    }

    public static void preInit() {
        instrumentSpecs = null;
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<InstrumentSpec>>(){}.getType();

        instrumentSpecs = gson.fromJson(new InputStreamReader(InstrumentConfig.class.getClassLoader().getResourceAsStream("data/mimi/instruments/default.json")), listType);
        try {
            List<InstrumentSpec> customInstruments = gson.fromJson(getCustomInstrumentJsonStream(FMLPaths.CONFIGDIR.get()), listType);
            if(!customInstruments.isEmpty() && customInstruments != null) {
                for(int i = 0; i < customInstruments.size(); i++) {
                    if(Integer.valueOf(instrumentSpecs.size() + i).byteValue() <= Byte.MAX_VALUE) {
                        customInstruments.get(i).instrumentId = Integer.valueOf(instrumentSpecs.size() + i).byteValue();
                    } else {
                        throw new IllegalStateException("Failed to load custom instrument specs! Instrument limit of 128 reached!");
                    }
                }
                instrumentSpecs.addAll(customInstruments);
            }
        } catch(Exception e) {
            throw new IllegalStateException("Failed to load custom instrument specs!", e);
        }
    }

    public static InstrumentSpec getBydId(byte id) {
        for(InstrumentSpec i : getAllInstruments()) {
            if(i.instrumentId.equals(id)) return i;
        }
        return null;
    }
}
