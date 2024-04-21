package io.github.tofodroid.mods.mimi.neoforge.common.config;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.neoforged.neoforge.common.ModConfigSpec;

// 1. Default MIDI Input Device

public class CommonConfig {
    public static final String WORLD_CATEGORY_NAME = "World Generation";
    public static final String MOB_CATEGORY_NAME = "Mobs";
    public static final String MUSIC_PLAYER_CATEGORY_NAME = "Server Music";

    public ModConfigSpec.BooleanValue enableInstrumentalistShop;
    public ModConfigSpec.ConfigValue<String> allowedInstrumentMobs;

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.push(WORLD_CATEGORY_NAME);
        enableInstrumentalistShop = builder.comment("Toggles whether Instrumentalist shops should generate in villages.")
            .translation(MIMIMod.MODID + ".config.server.world.village.instrumentalist")
            .define("enableInstrumentalistShop", true);
        builder.pop();
        builder.push(WORLD_CATEGORY_NAME);
        allowedInstrumentMobs = builder.comment("Comma-separated list of mobs that can be given instruments to hold")
            .translation(MIMIMod.MODID + ".config.server.mobs.allowed.instruments")
            .define("allowedInstrumentMobs", "minecraft:zombie,minecraft:husk,minecraft:skeleton,minecraft:stray,minecraft:wither_skeleton");
        builder.pop();
    }
}
