package io.github.tofodroid.mods.mimi.forge.common.config;

import java.nio.file.Path;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.loading.FMLPaths;

public class ModConfigs {
    public static ClientConfig CLIENT;
    private static ForgeConfigSpec CLIENTSPEC;
    public static CommonConfig COMMON;
    private static ForgeConfigSpec COMMONSPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENTSPEC = clientPair.getRight();

        final Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = commonPair.getLeft();
        COMMONSPEC = commonPair.getRight();
    }

    public static void registerConfigs() { 
        ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfigs.CLIENTSPEC);
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfigs.COMMONSPEC);
    }

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}
