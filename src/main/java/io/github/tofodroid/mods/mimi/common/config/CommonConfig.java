package io.github.tofodroid.mods.mimi.common.config;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
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
    }
}
