package io.github.tofodroid.mods.mimi.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraftforge.common.ForgeConfigSpec;

// 1. Default MIDI Input Device

public class CommonConfig {
    public static final String WORLD_CATEGORY_NAME = "World Generation";
    public static final String MUSIC_PLAYER_CATEGORY_NAME = "Server Music";

    public ForgeConfigSpec.BooleanValue enableInstrumentalistShop;
    public ForgeConfigSpec.IntValue serverMusicCacheSize;
    public ForgeConfigSpec.BooleanValue allowWebMidi;
    public ForgeConfigSpec.BooleanValue allowWebCommands;
    public ForgeConfigSpec.BooleanValue allowServerCommands;
    public ForgeConfigSpec.ConfigValue<String> allowedMusicHosts;
    protected List<String> allowedHostsList = null;

    public CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push(WORLD_CATEGORY_NAME);
        enableInstrumentalistShop = builder.comment("Toggles whether Instrumentalist shops should generate in villages.")
            .translation(MIMIMod.MODID + ".config.server.world.village.instrumentalist")
            .define("enableInstrumentalistShop", true);
        builder.pop();
        builder.push(MUSIC_PLAYER_CATEGORY_NAME);
        serverMusicCacheSize = builder.comment("Server music cache limit. The maximum number of MIDI files (from Floppy Disks) to keep in the server-side music cache.")
            .translation(MIMIMod.MODID + ".config.server.cache.music.size")
            .defineInRange("serverMusicCacheSize", 100, 0, 999);
        allowWebMidi = builder.comment("Whether or not to allow web URLs for Floppy Disks.")
            .translation(MIMIMod.MODID + ".config.server.music.allowed.hosts")
            .define("allowWebMidi", true);
        allowedMusicHosts = builder.comment("Allowed web hosts for Floppy Disk URLs (comma-separated). If no hosts are specified any host is allowed. Hosts should not include the protocol (I.E: https) or any paths (I.E: /path). Ex: bitmidi.com")
            .translation(MIMIMod.MODID + ".config.server.music.allowed.hosts")
            .define("allowedMusicHosts", "");
        allowWebCommands = builder.comment("Wether or not the server should allow the above web MIDI settings to be modified via in-game commands by players with 'op' privileges.")
            .translation(MIMIMod.MODID + ".config.server.command.allow.web")
            .define("allowWebCommands", true);
        allowServerCommands = builder.comment("Whether or not the server should allow the list of server-provided MIDI Files to be modified via in-game commands by players with 'op' privileges.")
            .translation(MIMIMod.MODID + ".config.server.command.allow.server")
            .define("allowServerCommands", true);
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

        return new ArrayList<>(allowedHostsList);
    }

    public void onConfigChange() {
        clearAllowedHostsList();
        if(allowWebMidi.get()) {
            ServerMusicPlayerMidiManager.revalidate();
        } else {
            ServerMusicPlayerMidiManager.clearMusicPlayers();
        }
    }

    public void addAllowedHost(String host) {
        List<String> allowedHosts = getAllowedHostsList();
        allowedHosts.add(host);
        allowedMusicHosts.set(allowedHosts.stream().collect(Collectors.joining(",")));
        onConfigChange();
    }

    public void removeAllowedHost(Integer index) {
        List<String> allowedHosts = getAllowedHostsList();
        allowedHosts.remove(index.intValue());

        if(allowedHosts.isEmpty()) {
            allowedMusicHosts.set("");
        } else {
            allowedMusicHosts.set(allowedHosts.stream().collect(Collectors.joining(",")));
        }
        onConfigChange();
    }

    public void clearAllowedHostsList() {
        this.allowedHostsList = null;
    }
}
