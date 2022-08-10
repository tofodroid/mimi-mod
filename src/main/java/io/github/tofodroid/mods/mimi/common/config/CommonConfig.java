package io.github.tofodroid.mods.mimi.common.config;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraftforge.common.ForgeConfigSpec;

// 1. Default MIDI Input Device

public class CommonConfig {
    public static final String MUSIC_PLAYER_CATEGORY_NAME = "Music Player";

    public ForgeConfigSpec.IntValue serverMusicCacheSize;
    public ForgeConfigSpec.ConfigValue<String> allowedMusicHosts;

    public CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push(MUSIC_PLAYER_CATEGORY_NAME);
        serverMusicCacheSize = builder.comment("Server music cache limit. The maximum number of MIDI files (from Floppy Disks) to keep in the server-side music cache.")
            .translation(MIMIMod.MODID + ".config.server.cache.music.size")
            .defineInRange("serverMusicCacheSize", 2, 0, 999);
        allowedMusicHosts = builder.comment("Allowed web hosts for Floppy Disk URLs (comma-separated). If no hosts are specified any host is allowed.")
            .translation(MIMIMod.MODID + ".config.server.music.allowed.hosts")
            .define("allowedMusicHosts", "");
        builder.pop();
    }
}
