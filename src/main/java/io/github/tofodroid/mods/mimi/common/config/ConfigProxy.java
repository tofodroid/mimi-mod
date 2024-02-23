package io.github.tofodroid.mods.mimi.common.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import io.github.tofodroid.mods.mimi.forge.common.config.ModConfigs;

public class ConfigProxy {
    public static Path getConfigPath() {
        return ModConfigs.getConfigPath();   
    }

    public static Boolean isInstrumentalistShopEnabled() {
        return ModConfigs.COMMON.enableInstrumentalistShop.get();
    }

    public static List<String> getAllowedInstrumentMobs() {
        return Arrays.asList(ModConfigs.COMMON.allowedInstrumentMobs.get().split(","));
    }

    public static void registerConfigs() {
        ModConfigs.registerConfigs();
    }
}
