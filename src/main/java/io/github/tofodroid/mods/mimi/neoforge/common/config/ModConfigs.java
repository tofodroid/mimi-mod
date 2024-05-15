package io.github.tofodroid.mods.mimi.neoforge.common.config;

import java.nio.file.Path;

import org.apache.commons.lang3.tuple.Pair;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfigs {
    public static ClientConfig CLIENT;
    private static ModConfigSpec CLIENTSPEC;
    public static CommonConfig COMMON;
    private static ModConfigSpec COMMONSPEC;

    static {
        final Pair<ClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENTSPEC = clientPair.getRight();

        final Pair<CommonConfig, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = commonPair.getLeft();
        COMMONSPEC = commonPair.getRight();
    }

    public static void registerConfigs() { 
        ModLoadingContext.get().getActiveContainer().registerConfig(Type.CLIENT, ModConfigs.CLIENTSPEC);
        ModLoadingContext.get().getActiveContainer().registerConfig(Type.COMMON, ModConfigs.COMMONSPEC);
    }

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}
