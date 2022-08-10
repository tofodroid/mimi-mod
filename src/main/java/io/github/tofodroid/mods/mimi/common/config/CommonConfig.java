package io.github.tofodroid.mods.mimi.common.config;

import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraftforge.common.ForgeConfigSpec;

// 1. Default MIDI Input Device

public class CommonConfig {
    public static final String MUSIC_PLAYER_CATEGORY_NAME = "Music Player";

    public ForgeConfigSpec.IntValue serverMusicCacheSize;
    public ForgeConfigSpec.BooleanValue allowWebMidi;
    public ForgeConfigSpec.ConfigValue<String> allowedMusicHosts;
    protected List<String> allowedHostsList = null;

    public CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push(MUSIC_PLAYER_CATEGORY_NAME);
        serverMusicCacheSize = builder.comment("Server music cache limit. The maximum number of MIDI files (from Floppy Disks) to keep in the server-side music cache.")
            .translation(MIMIMod.MODID + ".config.server.cache.music.size")
            .defineInRange("serverMusicCacheSize", 100, 0, 999);
        allowWebMidi = builder.comment("Whether or not to allow web URL for Floppy Disks.")
            .translation(MIMIMod.MODID + ".config.server.music.allowed.hosts")
            .define("allowWebMidi", true);
        allowedMusicHosts = builder.comment("Allowed web hosts for Floppy Disk URLs (comma-separated). If no hosts are specified any host is allowed. Hosts should not include the protocol (I.E: https) or any paths (I.E: /path). Ex: bitmidi.com")
            .translation(MIMIMod.MODID + ".config.server.music.allowed.hosts")
            .define("allowedMusicHosts", "");
        builder.pop();
    }

    public List<String> getAllowedHostsList() {
        if(allowedHostsList == null) {
            String allowedHostsRaw = ModConfigs.COMMON.allowedMusicHosts.get();
            if(allowedHostsRaw != null && !allowedHostsRaw.isBlank() && !allowedHostsRaw.trim().equals(",")) {
                allowedHostsList = List.of(allowedHostsRaw.split(",")).stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
            } else {
                allowedHostsList = List.of();
            }
        }

        return allowedHostsList;        
    }

    public void clearAllowedHostsList() {
        this.allowedHostsList = null;
    }
}
