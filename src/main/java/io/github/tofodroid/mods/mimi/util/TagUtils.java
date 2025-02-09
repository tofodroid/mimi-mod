package io.github.tofodroid.mods.mimi.util;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.UnaryOperator;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import com.mojang.serialization.Codec;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class TagUtils {
    private static final HashMap<String, DataComponentType<Byte>> BYTE_COMPONENTS = new HashMap<>();
    private static final HashMap<String, DataComponentType<Integer>> INT_COMPONENTS = new HashMap<>();
    private static final HashMap<String, DataComponentType<Boolean>> BOOL_COMPONENTS = new HashMap<>();
    private static final HashMap<String, DataComponentType<String>> STRING_COMPONENTS = new HashMap<>();
    private static final HashMap<String, DataComponentType<UUID>> UUID_COMPONENTS = new HashMap<>();
    private static final HashMap<String, DataComponentType<CompoundTag>> NBT_COMPONENTS = new HashMap<>();

    public static final HashMap<ResourceLocation, DataComponentType<?>> COMPONENT_TYPES = new HashMap<>();

    static {
        // Other
        createBoolComponent("inverted");
        createIntComponent("dye_id");
        
        // Playlist
        createNbtComponent("favorite_songs");
        createBoolComponent("loop_mode");
        createBoolComponent("favorite_mode");
        createBoolComponent("source_mode");
        createIntComponent("shuffle");

        // MIDI
        createByteComponent("filter_note");
        createByteComponent("filter_oct");
        createBoolComponent("invert_note_oct");
        createByteComponent("broadcast_note");
        createUUIDComponent("source_uuid");
        createStringComponent("source_name");
        createBoolComponent("sys_input");
        createIntComponent("channels");
        createByteComponent("filter_instrument");
        createBoolComponent("invert_instrument");
        createByteComponent("instrument_volume");
        createBoolComponent("invert_signal");
        createBoolComponent("note_start");
        createByteComponent("hold_ticks");
        createByteComponent("broadcast_range");

        // Effect Emitter
        createStringComponent("sound");
        createStringComponent("particle");
        createByteComponent("volume");
        createByteComponent("pitch");
        createByteComponent("side");
        createByteComponent("spread");
        createByteComponent("count");
        createByteComponent("speed_x");
        createByteComponent("speed_y");
        createByteComponent("speed_z");
        createByteComponent("particle_loop");
        createByteComponent("sound_loop");

        // Relay
        for(byte i = 0; i < 16; i++) {
            createByteComponent("channel_map_" + i);
        }
    }

    // Register
    private static <T> DataComponentType<T> register(String pName, UnaryOperator<DataComponentType.Builder<T>> pBuilder) {
        DataComponentType<T> component = pBuilder.apply(DataComponentType.builder()).build();
        COMPONENT_TYPES.put(ResourceUtils.newModLocation(pName), component);
        return component;
    }
    
    // Create
    private static final DataComponentType<Byte> createByteComponent(String tag) {
        return BYTE_COMPONENTS.computeIfAbsent(tag, (newTag) -> register(tag, builder -> builder
            .persistent(Codec.BYTE)
            .networkSynchronized(ByteBufCodecs.BYTE))
        );
    }

    private static final DataComponentType<Integer> createIntComponent(String tag) {
        return INT_COMPONENTS.computeIfAbsent(tag, (newTag) -> register(tag, builder -> builder
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT))
        );
    }

    private static final DataComponentType<Boolean> createBoolComponent(String tag) {
        return BOOL_COMPONENTS.computeIfAbsent(tag, (newTag) -> register(tag, builder -> builder
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL))
        );
    }

    private static final DataComponentType<String> createStringComponent(String tag) {
        return STRING_COMPONENTS.computeIfAbsent(tag, (newTag) -> register(tag, builder -> builder
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8))
        );
    }

    private static final DataComponentType<UUID> createUUIDComponent(String tag) {
        return UUID_COMPONENTS.computeIfAbsent(tag, (newTag) -> register(tag, builder -> builder
            .persistent(UUIDUtil.CODEC)
            .networkSynchronized(UUIDUtil.STREAM_CODEC))
        );
    }

    private static final DataComponentType<CompoundTag> createNbtComponent(String tag) {
        return NBT_COMPONENTS.computeIfAbsent(tag, (newTag) -> register(tag, builder -> builder
            .persistent(CompoundTag.CODEC)
            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG))
        );
    }

    // GET
    public static final DataComponentType<Byte> getByteComponent(String tag) {
        return BYTE_COMPONENTS.computeIfAbsent(tag, (newTag) -> {
            MIMIMod.LOGGER.error("Attempted to access unregistered byte component: " + tag);
            return null;
        });
    }

    public static final DataComponentType<Integer> getIntComponent(String tag) {
        return INT_COMPONENTS.computeIfAbsent(tag, (newTag) -> {
            MIMIMod.LOGGER.error("Attempted to access unregistered int component: " + tag);
            return null;
        });
    }

    public static final DataComponentType<Boolean> getBoolComponent(String tag) {
        return BOOL_COMPONENTS.computeIfAbsent(tag, (newTag) -> {
            MIMIMod.LOGGER.error("Attempted to access unregistered bool component: " + tag);
            return null;
        });
    }

    public static final DataComponentType<String> getStringComponent(String tag) {
        return STRING_COMPONENTS.computeIfAbsent(tag, (newTag) -> {
            MIMIMod.LOGGER.error("Attempted to access unregistered string component: " + tag);
            return null;
        });
    }

    public static final DataComponentType<UUID> getUUIDComponent(String tag) {
        return UUID_COMPONENTS.computeIfAbsent(tag, (newTag) -> {
            MIMIMod.LOGGER.error("Attempted to access unregistered uuid component: " + tag);
            return null;
        });
    }

    public static final DataComponentType<CompoundTag> getNbtComponent(String tag) {
        return NBT_COMPONENTS.computeIfAbsent(tag, (newTag) -> {
            MIMIMod.LOGGER.error("Attempted to access unregistered nbt component: " + tag);
            return null;
        });
    }

    // WRAPPERS
    public static <T> T getOrDefault(DataComponentHolder stack, DataComponentType<T> type, T defaultVal)  {
        T result = stack.get(type);
        return result != null ? result : defaultVal;
    }

    public static CompoundTag getNbtOrDefault(DataComponentHolder stack, String tag, CompoundTag defaultVal) {
        try {
            DataComponentType<CompoundTag> componentType = getNbtComponent(tag);
            return stack.has(componentType) ? stack.get(componentType) : defaultVal;
        } catch(Exception e){}
        return defaultVal;
    }

    public static UUID getUUIDOrDefault(DataComponentHolder stack, String tag, UUID defaultVal) {
        try {
            DataComponentType<UUID> componentType = getUUIDComponent(tag);
            return stack.has(componentType) ? stack.get(componentType) : defaultVal;
        } catch(Exception e){}
        return defaultVal;
    }

    public static Integer getIntOrDefault(DataComponentHolder stack, String tag, Integer defaultVal) {
        try {
            DataComponentType<Integer> componentType = getIntComponent(tag);
            return stack.has(componentType) ? stack.get(componentType) : defaultVal;
        } catch(Exception e){}
        return defaultVal;
    }

    public static Byte getByteOrDefault(DataComponentHolder stack, String tag, Byte defaultVal) {
        try {
            DataComponentType<Byte> componentType = getByteComponent(tag);
            return stack.has(componentType) ? stack.get(componentType) : defaultVal;
        } catch(Exception e){}
        return defaultVal;
    }

    public static Boolean getBooleanOrDefault(DataComponentHolder stack, String tag, Boolean defaultVal) {
        try {
            DataComponentType<Boolean> componentType = getBoolComponent(tag);
            return stack.has(componentType) ? stack.get(componentType) : defaultVal;
        } catch(Exception e){}
        return defaultVal;
    }

    public static String getStringOrDefault(DataComponentHolder stack, String tag, String defaultVal) {
        try {
            DataComponentType<String> componentType = getStringComponent(tag);
            return stack.has(componentType) ? stack.get(componentType) : defaultVal;
        } catch(Exception e){}
        return defaultVal;
    }

    public static void setOrRemoveNbt(ItemStack stack, String tag, CompoundTag value) {
        DataComponentType<CompoundTag> componentType = getNbtComponent(tag);
        if (value != null) {
            stack.set(componentType, value);
        } else if (stack.has(componentType)) {
            stack.remove(componentType);
        }
    }

    public static void setOrRemoveUUID(ItemStack stack, String tag, UUID value) {
        DataComponentType<UUID> componentType = getUUIDComponent(tag);
        if (value != null) {
            stack.set(componentType, value);
        } else if (stack.has(componentType)) {
            stack.remove(componentType);
        }
    }

    public static void setOrRemoveInt(ItemStack stack, String tag, Integer value) {
        DataComponentType<Integer> componentType = getIntComponent(tag);
        if (value != null) {
            stack.set(componentType, value);
        } else if (stack.has(componentType)) {
            stack.remove(componentType);
        }
    }

    public static void setOrRemoveByte(ItemStack stack, String tag, Byte value) {
        DataComponentType<Byte> componentType = getByteComponent(tag);
        if (value != null) {
            stack.set(componentType, value);
        } else if (stack.has(componentType)) {
            stack.remove(componentType);
        }
    }

    public static void setOrRemoveBoolean(ItemStack stack, String tag, Boolean value) {
        DataComponentType<Boolean> componentType = getBoolComponent(tag);
        if (value != null) {
            stack.set(componentType, value);
        } else if (stack.has(componentType)) {
            stack.remove(componentType);
        }
    }

    public static void setOrRemoveString(ItemStack stack, String tag, String value) {
        DataComponentType<String> componentType = getStringComponent(tag);
        if (value != null) {
            stack.set(componentType, value);
        } else if (stack.has(componentType)) {
            stack.remove(componentType);
        }
    }
    
    public static Byte getByteOrDefault(ItemStack stack, String tag, Integer defaultVal) {
        return getByteOrDefault(stack, tag, defaultVal.byteValue());
    }

    public static Integer getIntOrDefault(ItemStack stack, String tag, Byte defaultVal) {
        return getIntOrDefault(stack, tag, defaultVal.intValue());
    }
}
