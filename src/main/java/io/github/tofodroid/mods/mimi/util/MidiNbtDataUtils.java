package io.github.tofodroid.mods.mimi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class MidiNbtDataUtils {
    public static final Byte ALL_CHANNELS = Byte.MAX_VALUE;
    public static final Byte FILTER_NOTE_OCT_ALL = -1;
    public static final Byte INSTRUMENT_ALL = -1;
    public static final Byte MAX_TRIGGER_MODE = 3;
    public static final Byte MAX_HOLD_TICKS = 20;
    public static final Byte MIN_BROADCAST_RANGE = 1;
    public static final Byte MAX_BROADCAST_RANGE = 4;
    public static final Byte MAX_INSTRUMENT_VOLUME = 10;
    public static final Integer PERCUSSION_BANK = 120;
    public static final Byte DEFAULT_INSTRUMENT_VOLUME = 5;
    public static final Byte MIN_INSTRUMENT_VOLUME = 0;
    public static final Integer ALL_CHANNELS_INT = 65535;
    public static final Integer ALL_BUT_10_CHANNELS_INT = 65023;
    public static final Integer JUST_CHANNEL_10_INT = 512;
    public static final Integer NONE_CHANNELS_INT = 0;
    public static final String TRANSMITTER_SOURCE_PREFIX = ";T;";
    public static final String RELAY_SOURCE_PREFIX = ";R;";

    public static final String FILTER_NOTE_TAG = "filter_note";
    public static final String FILTER_OCT_TAG = "filter_oct";
    public static final String INVERT_NOTE_OCT_TAG = "invert_note_oct";
    public static final String BROADCAST_NOTE_TAG = "broadcast_note";
    public static final String SOURCE_TAG = "source_uuid";
    public static final String SOURCE_NAME_TAG = "source_name";
    public static final String SYS_INPUT_TAG = "sys_input";
    public static final String ENABLED_CHANNELS_TAG = "channels";
    public static final String INSTRUMENT_TAG = "filter_instrument";
    public static final String VOLUME_TAG = "instrument_volume";
    public static final String INVERT_SIGNAL_TAG = "invert_signal";
    public static final String NOTE_START_TRIGGER_TAG = "note_start";
    public static final String HOLD_TICKS_TAG = "hold_ticks";
    public static final String BROADCAST_RANGE_TAG = "broadcast_range";
    public static final String CHANNEL_MAP_BASE_TAG = "channel_map_";
    
    private static Map<Byte,String> INSTRUMENT_NAME_MAP = null;

    protected static Map<Byte,String> loadInstrumentNames() {
        Map<Byte,String> result = new HashMap<>();
        result.put(Integer.valueOf(-1).byteValue(), "All");
        ModItems.INSTRUMENT_ITEMS.forEach(item -> {
            result.put(item.getInstrumentId(), item.getDescription().getString());
        });
        ModItems.BLOCK_INSTRUMENT_ITEMS.forEach(item -> {
            result.put(item.getInstrumentId(), item.getBlock().getName().getString());
        });
        return result;
    }

    public static Map<Byte,String> INSTRUMENT_NAME_MAP() {
        if(INSTRUMENT_NAME_MAP == null) {
            INSTRUMENT_NAME_MAP = loadInstrumentNames();
        }

        return INSTRUMENT_NAME_MAP;
    }

    public static void setMidiSourceFromRelay(ItemStack stack, UUID sourceId, String sourceName) {
        setMidiSource(stack, sourceId, RELAY_SOURCE_PREFIX + sourceName);
    }

    public static void setMidiSourceFromTransmitter(ItemStack stack,UUID sourceId, String sourceName) {
        setMidiSource(stack, sourceId, TRANSMITTER_SOURCE_PREFIX + sourceName);
    }

    public static void setMidiSource(ItemStack stack, UUID sourceId, String sourceName) {
        TagUtils.setOrRemoveUUID(stack, SOURCE_TAG, sourceId);
        TagUtils.setOrRemoveString(stack, SOURCE_NAME_TAG, sourceName);
    }

    public static UUID getMidiSource(ItemStack stack) {
        return TagUtils.getUUIDOrDefault(stack, SOURCE_TAG, null);
    }

    public static Boolean getMidiSourceIsTransmitter(ItemStack stack) {
        return TagUtils.getStringOrDefault(stack, SOURCE_NAME_TAG, "").startsWith(TRANSMITTER_SOURCE_PREFIX);
    }

    public static Boolean getMidiSourceIsRelay(ItemStack stack) {
        return TagUtils.getStringOrDefault(stack, SOURCE_NAME_TAG, "").startsWith(RELAY_SOURCE_PREFIX);
    }

    public static String getMidiSourceName(ItemStack stack, Boolean forDisplay) {
        UUID sourceId = getMidiSource(stack);

        if(sourceId == null) {
            return "None";
        }

        String name = TagUtils.getStringOrDefault(stack, SOURCE_NAME_TAG, "Unknown");
        return forDisplay ? name.replaceFirst(TRANSMITTER_SOURCE_PREFIX, "").replaceFirst(RELAY_SOURCE_PREFIX, "") : name;
    }

    public static void setEnabledChannelsInt(ItemStack stack, Integer enabledChannels) {
        TagUtils.setOrRemoveInt(stack, ENABLED_CHANNELS_TAG, enabledChannels);
    }

    public static Integer getEnabledChannelsInt(ItemStack stack) {
        return TagUtils.getIntOrDefault(stack, ENABLED_CHANNELS_TAG, getDefaultChannelsInt(stack));
    }

    public static Integer getDefaultChannelsInt(ItemStack stack) {
        if(stack.getItem() instanceof IInstrumentItem) {
            return ((IInstrumentItem)stack.getItem()).getDefaultChannels();
        } else if(stack.getItem().equals(ModItems.RELAY)) {
            return ALL_CHANNELS_INT;
        }
        return NONE_CHANNELS_INT;
    }

    public static void toggleChannel(ItemStack switchStack, Byte channelId) {
        if(channelId != null && channelId < 16 && channelId >= 0) {
            Integer enabledChannels = getEnabledChannelsInt(switchStack);
            setEnabledChannelsInt(switchStack, enabledChannels ^ (1 << channelId));
        }
    }
    
    public static void clearEnabledChannels(ItemStack switchStack) {
        setEnabledChannelsInt(switchStack, NONE_CHANNELS_INT);
    }
    
    public static void setEnableAllChannels(ItemStack switchStack) {
        setEnabledChannelsInt(switchStack, ALL_CHANNELS_INT);
    }

    public static Boolean isChannelEnabled(ItemStack switchStack, Byte channelId) {
        return isChannelEnabled(getEnabledChannelsInt(switchStack), channelId);
    }
    
    public static Boolean isChannelEnabled(Integer enabledChannels, Byte channelId) {
        return ((enabledChannels >> channelId) & 1) == 1;
    }

    public static String getEnabledChannelsAsString(Integer enabledChannels) {
        String result = "";

        for(byte i = 0; i < 16; i++) {
            if(((enabledChannels >> i) & 1) == 1) {
                result += (i+1) + ",";
            }
        }
        return result.isEmpty() ? result : result.substring(0, result.length()-1);
    }
    
    public static List<Byte> getEnabledChannelsList(ItemStack switchStack) {
        List<Byte> result = new ArrayList<>();
        Integer enabledChannels = getEnabledChannelsInt(switchStack);

        for(byte i = 0; i < 16; i++) {
            if(((enabledChannels >> i) & 1) == 1) {
                result.add(i);
            }
        }

        return result;
    }

    public static void setFilterOct(ItemStack stack, Byte oct) {
        TagUtils.setOrRemoveByte(stack, FILTER_OCT_TAG, oct >= 0 ? oct : null);
    }
    
    public static void setFilterNote(ItemStack stack, Byte note) {
        TagUtils.setOrRemoveByte(stack, FILTER_NOTE_TAG, note >= 0 ? note : null);
    }

    public static Byte getFilterOct(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, FILTER_OCT_TAG, FILTER_NOTE_OCT_ALL);
    }

    public static Byte getFilterNote(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, FILTER_NOTE_TAG, FILTER_NOTE_OCT_ALL);
    }

    public static void setBroadcastNote(ItemStack stack, Byte note) {
        TagUtils.setOrRemoveByte(stack, BROADCAST_NOTE_TAG, note >= 0 ? note : null);
    }

    public static Byte getBroadcastNote(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, BROADCAST_NOTE_TAG, 0);
    }

    public static Byte getInstrumentId(ItemStack stack) {
        if(stack.getItem() instanceof IInstrumentItem) {
            return ((IInstrumentItem)stack.getItem()).getInstrumentId();
        }
        return null;
    }

    public static void setFilterInstrument(ItemStack stack, Byte instrumentId) {
        TagUtils.setOrRemoveByte(stack, INSTRUMENT_TAG, instrumentId >= 0 ? instrumentId : null);
    }

    public static Byte getFilterInstrument(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, INSTRUMENT_TAG, INSTRUMENT_ALL);
    }
    
    public static void setSysInput(ItemStack stack, Boolean sysInput) {
        TagUtils.setOrRemoveBoolean(stack, SYS_INPUT_TAG, sysInput ? sysInput : null);
    }

    public static Boolean getSysInput(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, SYS_INPUT_TAG, false);
    }
    
    public static void setInvertNoteOct(ItemStack stack, Boolean invert) {
        TagUtils.setOrRemoveBoolean(stack, INVERT_NOTE_OCT_TAG, invert ? invert : null);
    }

    public static Boolean getInvertNoteOct(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, INVERT_NOTE_OCT_TAG, false);
    }

    public static Boolean getInvertSignal(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, INVERT_SIGNAL_TAG, false);
    }

    public static void setInvertSignal(ItemStack stack, Boolean invert) {
        TagUtils.setOrRemoveBoolean(stack, INVERT_SIGNAL_TAG, invert ? invert : null);
    }
    public static Boolean getTriggerNoteStart(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, NOTE_START_TRIGGER_TAG, false);
    }

    public static void setTriggerNoteStart(ItemStack stack, Boolean held) {
        TagUtils.setOrRemoveBoolean(stack, NOTE_START_TRIGGER_TAG, held ? held : null);
    }

    public static void setHoldTicks(ItemStack stack, Byte holdTicks) {
        if(holdTicks > MAX_HOLD_TICKS) {
            holdTicks = MAX_HOLD_TICKS;
        } else if(holdTicks < 1) {
            holdTicks = 1;
        }
        TagUtils.setOrRemoveByte(stack, HOLD_TICKS_TAG, holdTicks);
    }

    public static Byte getHoldTicks(ItemStack stack) {
        Byte ticks = TagUtils.getByteOrDefault(stack, HOLD_TICKS_TAG, 1);
        return ticks < 1 ? 1 : ticks;
    }

    public static void setBroadcastRange(ItemStack stack, Byte broadcastRange) {
        if(broadcastRange > MAX_BROADCAST_RANGE) {
            broadcastRange = MAX_BROADCAST_RANGE;
        } else if(broadcastRange < MIN_BROADCAST_RANGE) {
            broadcastRange = MIN_BROADCAST_RANGE;
        }
        TagUtils.setOrRemoveByte(stack, BROADCAST_RANGE_TAG, broadcastRange);
    }

    public static Byte getBroadcastRange(ItemStack stack) {
        Byte range = TagUtils.getByteOrDefault(stack, BROADCAST_RANGE_TAG, 1);
        return range < MIN_BROADCAST_RANGE ? MIN_BROADCAST_RANGE : range;
    }

    public static void setInstrumentVolume(ItemStack stack, Byte volume) {
        if(volume > MAX_INSTRUMENT_VOLUME) {
            volume = MAX_INSTRUMENT_VOLUME;
        } else if(volume < MIN_INSTRUMENT_VOLUME) {
            volume = MIN_INSTRUMENT_VOLUME;
        }
        TagUtils.setOrRemoveByte(stack, VOLUME_TAG, volume);
    }

    public static Byte getInstrumentVolume(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, VOLUME_TAG, DEFAULT_INSTRUMENT_VOLUME);
    }

    public static Byte applyInstrumentVolume(ItemStack stack, Byte sourceVelocity) {
        return applyVolume(getInstrumentVolume(stack), sourceVelocity);
    }

    public static Byte applyVolume(Byte volume, Byte sourceVelocity) {
        return Integer.valueOf(Double.valueOf((Double.valueOf(volume) / Double.valueOf(MAX_INSTRUMENT_VOLUME)) * sourceVelocity).intValue()).byteValue();
    }

    public static String getInstrumentName(Byte instrumentId) {
        return INSTRUMENT_NAME_MAP().get(instrumentId);
    }

    public static Byte[] getChannelMap(ItemStack stack) {
        Byte[] result = new Byte[16];
        for(byte i = 0; i < 16; i++) {
            result[i] = TagUtils.getByteOrDefault(stack, CHANNEL_MAP_BASE_TAG + i, i);
        }
        return result;
    }

    public static Byte getChannelMap(ItemStack stack, Byte index) {
        return TagUtils.getByteOrDefault(stack, CHANNEL_MAP_BASE_TAG + index, index);
    }

    public static void setChannelMap(ItemStack stack, Byte[] map) {
        for(byte i = 0; i < 16; i++) {
            setChannelMap(stack, i, map[i]);
        }
    }

    public static void setChannelMap(ItemStack stack, Byte index, Byte value) {
        TagUtils.setOrRemoveByte(stack, CHANNEL_MAP_BASE_TAG + index, value == index.byteValue() ? null : (value < 0 ? 0 : (value > 15 ? 15 : value)));
    }

    public static Integer getDefaultChannelsForBank(Integer bankNumber) {
        if(bankNumber == PERCUSSION_BANK) {
            return JUST_CHANNEL_10_INT;
        }
        return ALL_BUT_10_CHANNELS_INT;
    }
    
    public static String getFilteredNotesAsString(ItemStack stack) {
        Byte filterNoteLetter = getFilterNote(stack);
        Byte filterNoteOctave = getFilterOct(stack);
        String filterNoteString = noteLetterFromNum(filterNoteLetter) + (filterNoteOctave != FILTER_NOTE_OCT_ALL ? filterNoteOctave : "*");
        return "**".equals(filterNoteString) ? "All" : filterNoteString;
    }

    public static String getMidiNoteAsString(Byte note) {
        String result = "None";

        if(note != null) {
            Byte filterNoteLetter = Integer.valueOf(note % 12).byteValue();
            Byte filterNoteOctave = Integer.valueOf(note / 12).byteValue();
            result = noteLetterFromNum(filterNoteLetter) + filterNoteOctave;
        }

        return result;
    }

    public static String noteLetterFromNum(Byte octaveNoteNum) {
        switch(octaveNoteNum) {
            case -1:
                return "*";
            case 0:
                return "C";
            case 1:
                return "C#";
            case 2:
                return "D";
            case 3:
                return "D#";
            case 4:
                return "E";
            case 5:
                return "F";
            case 6:
                return "F#";
            case 7:
                return "G";
            case 8:
                return "G#";
            case 9:
                return "A";
            case 10:
                return "A#";
            case 11:
                return "B";
        }

        return "";
    }

    public static Boolean isNoteFiltered(Byte filterNote, Integer filterOctMin, Integer filterOctMax, Boolean invertNoteOct, Byte note) {
        Boolean isFiltered = true;

        if(filterOctMin >= 0) {
            isFiltered = note >= filterOctMin && note < filterOctMax;
        }

        if(isFiltered && filterNote >= 0) {
            isFiltered = note % 12 == filterNote;
        }

        return invertNoteOct ? !isFiltered : isFiltered;
    }

    public static CompoundTag convertSwitchboardToDataTag(CompoundTag switchTag) {
        CompoundTag instrumentTag = switchTag != null ? switchTag.copy() : new CompoundTag();

        // OLD DEFAULT CHANNEL --> NONE
        if(!instrumentTag.contains("enabled_channels") || instrumentTag.getString("enabled_channels").isBlank()) {
            instrumentTag.putInt(ENABLED_CHANNELS_TAG, NONE_CHANNELS_INT);
        }
        
        // OLD ENABLED CHANNELS STRING --> ENABLED CHANNELS INT
        if(instrumentTag.contains("enabled_channels")) {
            Integer enabledChannelsInt = Arrays.asList(instrumentTag.getString("enabled_channels").split(",", -1)).stream()
                .map(s -> Integer.valueOf(Byte.valueOf(s) - 1))
                .reduce(NONE_CHANNELS_INT, (enabledChannels, channelId) -> enabledChannels ^ (1 << channelId));
            instrumentTag.putInt(ENABLED_CHANNELS_TAG, enabledChannelsInt);     
            instrumentTag.remove("enabled_channels");       
        }

        // OLD DEFAULT SOURCE ID OR PUBLIC SOURCE ID --> NONE
        final UUID NONE_SOURCE_ID = new UUID(0,0);
        final UUID PUBLIC_SOURCE_ID = new UUID(0,2);

        if(instrumentTag.contains(SOURCE_TAG) && (instrumentTag.getUUID(SOURCE_TAG).equals(NONE_SOURCE_ID) || instrumentTag.getUUID(SOURCE_TAG).equals(PUBLIC_SOURCE_ID))) {
            instrumentTag.remove(SOURCE_TAG);
            instrumentTag.remove(SOURCE_NAME_TAG);
        }

        // OLD DEFAULT SOURCE NAME --> NONE
        if(instrumentTag.contains(SOURCE_NAME_TAG) && instrumentTag.getString(SOURCE_NAME_TAG).isBlank()) {
            instrumentTag.remove(SOURCE_NAME_TAG);
        }

        return instrumentTag;
    }

    public static void appendBroadcastRangeTooltip(ItemStack stack, List<Component> tooltip) {
        int broadcastRange = MidiNbtDataUtils.getBroadcastRange(stack);
        String rangeDisplay = broadcastRange + "/" + MAX_BROADCAST_RANGE;
        tooltip.add(Component.literal("  Range: " + rangeDisplay).withStyle(ChatFormatting.GREEN));
    }

    public static void appendEnabledChannelsTooltip(ItemStack stack, List<Component> tooltip) {
        Integer enabledChannels = MidiNbtDataUtils.getEnabledChannelsInt(stack);
        if(enabledChannels != null) {
            if(enabledChannels.equals(MidiNbtDataUtils.ALL_CHANNELS_INT)) {
                tooltip.add(Component.literal("  Channels: All").withStyle(ChatFormatting.GREEN));
            } else if(enabledChannels.equals(MidiNbtDataUtils.NONE_CHANNELS_INT)) {
                tooltip.add(Component.literal("  Channels: None").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("  Channels: " + MidiNbtDataUtils.getEnabledChannelsAsString(enabledChannels)).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    public static void appendMidiChannelMappingsTooltip(ItemStack stack, List<Component> tooltip) {
        Integer enabledChannels = MidiNbtDataUtils.getEnabledChannelsInt(stack);
        tooltip.add(Component.literal("  Channels:").withStyle(ChatFormatting.GREEN));

        if(enabledChannels.equals(MidiNbtDataUtils.NONE_CHANNELS_INT)) {
            tooltip.add(Component.literal("    None").withStyle(ChatFormatting.GREEN));
        } else {
            Byte[] channelMap = MidiNbtDataUtils.getChannelMap(stack);
            Boolean channelRendered = false;
            for(byte i = 0; i < 16; i++) {
                Boolean enabled = MidiNbtDataUtils.isChannelEnabled(enabledChannels, i);
                
                if(!enabled || channelMap[i] != i) {
                    tooltip.add(Component.literal("    " + (i+1) + " (" + (enabled ? "On" : "Off") + ") --> " + (channelMap[i]+1)).withStyle(ChatFormatting.GREEN));
                    channelRendered = true;
                }
            }

            if(!channelRendered) {
                tooltip.add(Component.literal("    Default").withStyle(ChatFormatting.GREEN));
            }
        }
    }

    public static Component getMidiSourceType(ItemStack stack) {
        if(MidiNbtDataUtils.getMidiSource(stack) != null) {
            Boolean isTransmitter = MidiNbtDataUtils.getMidiSourceIsTransmitter(stack);
            Boolean isRelay = !isTransmitter && MidiNbtDataUtils.getMidiSourceIsRelay(stack);
            return (isTransmitter ? ModBlocks.TRANSMITTERBLOCK.getName() : ( isRelay ? ModBlocks.RELAY.getName() : Component.literal("Player")));
        }
        return Component.literal("None");
    }

    public static void appendMidiSourceTooltip(ItemStack stack, List<Component> tooltip) {
        if(MidiNbtDataUtils.getMidiSource(stack) != null) {
            tooltip.add(Component.literal("  Receive Notes From: ").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  ").append(getMidiSourceType(stack)).append(Component.literal(":")).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            tooltip.add(Component.literal("    " + MidiNbtDataUtils.getMidiSourceName(stack, true)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("  Receive Notes From: None").withStyle(ChatFormatting.GREEN));
        }
    }

    public static void appendInstrumentVolumeTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal("  Volume: " + MidiNbtDataUtils.getInstrumentVolume(stack)).withStyle(ChatFormatting.GREEN));
    }

    public static void appendDeviceInputEnabledTooltip(ItemStack stack, List<Component> tooltip) {
        if(MidiNbtDataUtils.getSysInput(stack)) {
            tooltip.add(Component.literal("  Device Input: Enabled").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("  Device Input: Disabled").withStyle(ChatFormatting.GREEN));
        }
    }

    public static void appendInvertSignalTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal("  Invert Power: "  + (MidiNbtDataUtils.getInvertSignal(stack) ? "Yes " : "No")).withStyle(ChatFormatting.GREEN));
    }

    public static void appendFilterNoteTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal("  Instrument: " + MidiNbtDataUtils.getInstrumentName(MidiNbtDataUtils.getFilterInstrument(stack))).withStyle(ChatFormatting.GREEN));
    }

    public static void appendFilterInstrumentTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal("  Note(s): " + (MidiNbtDataUtils.getInvertNoteOct(stack) ? "Not " : "")  + MidiNbtDataUtils.getFilteredNotesAsString(stack)).withStyle(ChatFormatting.GREEN));
    }

    public static ItemStack copyMidiSettings(ItemStack source, ItemStack target) {
        if(!source.isEmpty() && !target.isEmpty()) {
            ItemStack result = target.copyWithCount(1);
            MidiNbtDataUtils.setMidiSource(result, MidiNbtDataUtils.getMidiSource(source), MidiNbtDataUtils.getMidiSourceName(source, false));
            MidiNbtDataUtils.setEnabledChannelsInt(result, MidiNbtDataUtils.getEnabledChannelsInt(source));
            MidiNbtDataUtils.setChannelMap(result, MidiNbtDataUtils.getChannelMap(source));
            MidiNbtDataUtils.setBroadcastRange(result, MidiNbtDataUtils.getBroadcastRange(source));
            MidiNbtDataUtils.setSysInput(result, MidiNbtDataUtils.getSysInput(source));
            MidiNbtDataUtils.setInstrumentVolume(result, MidiNbtDataUtils.getInstrumentVolume(source));
            MidiNbtDataUtils.setFilterOct(result, MidiNbtDataUtils.getFilterOct(source));
            MidiNbtDataUtils.setFilterNote(result, MidiNbtDataUtils.getFilterNote(source));
            MidiNbtDataUtils.setInvertNoteOct(result, MidiNbtDataUtils.getInvertNoteOct(source));
            MidiNbtDataUtils.setFilterInstrument(result, MidiNbtDataUtils.getFilterInstrument(source));
            MidiNbtDataUtils.setInvertSignal(result, MidiNbtDataUtils.getInvertSignal(source));
            MidiNbtDataUtils.setTriggerNoteStart(result, MidiNbtDataUtils.getTriggerNoteStart(source));
            MidiNbtDataUtils.setHoldTicks(result, MidiNbtDataUtils.getHoldTicks(source));
            return result;
        }

        return target;
    }
}
