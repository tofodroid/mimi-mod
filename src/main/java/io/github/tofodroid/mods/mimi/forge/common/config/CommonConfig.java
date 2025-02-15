package io.github.tofodroid.mods.mimi.forge.common.config;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.common.ForgeConfigSpec;

// 1. Default MIDI Input Device

public class CommonConfig {
    public static final String WORLD_CATEGORY_NAME = "World Generation";
    public static final String MOB_CATEGORY_NAME = "Mobs";
    public static final String OTHER_CATEGORY_NAME = "Other";

    public ForgeConfigSpec.BooleanValue enableInstrumentalistShop;
    public ForgeConfigSpec.ConfigValue<String> allowedInstrumentMobs;
    public ForgeConfigSpec.BooleanValue doLogMidiTaskErrors;
    public ForgeConfigSpec.ConfigValue<Integer> midiTaskTimeoutMillis;

    public CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push(WORLD_CATEGORY_NAME);
        enableInstrumentalistShop = builder.comment("Toggles whether Instrumentalist shops should generate in villages.")
            .translation(MIMIMod.MODID + ".config.server.world.village.instrumentalist")
            .define("enableInstrumentalistShop", true);
        builder.pop();
        builder.push(WORLD_CATEGORY_NAME);
        allowedInstrumentMobs = builder.comment("Comma-separated list of mobs that can be given instruments to hold.")
            .translation(MIMIMod.MODID + ".config.server.mobs.allowed.instruments")
            .define("allowedInstrumentMobs", "minecraft:zombie,minecraft:husk,minecraft:skeleton,minecraft:stray,minecraft:wither_skeleton");
        builder.pop();
        builder.push(OTHER_CATEGORY_NAME);
        doLogMidiTaskErrors = builder.comment("Toggles whether MIMI should log when the server fails to timely execute a MIDI command which usually just indiciates server performance trouble.")
            .translation(MIMIMod.MODID + ".config.server.other.doLogMidiTaskErrors")
            .define("doLogMidiTaskErrors", true);
        midiTaskTimeoutMillis = builder.comment("The number of milliseconds a MIDI thread task should wait before timing out. Longer times may reduce instances of tasks failing but also reduce responsiveness of Transmitters.")
            .translation(MIMIMod.MODID + ".config.server.other.midiTaskTimeoutMillis")
            .define("doLogMidiTaskErrors", 15000);
    }
}
