package io.github.tofodroid.mods.mimi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.item.ItemStack;

public abstract class MidiNbtDataUtils {
    public static final Byte ALL_CHANNELS = Byte.MAX_VALUE;
    public static final String FILTER_NOTE_TAG = "filter_note";
    public static final String FILTER_OCT_TAG = "filter_oct";
    public static final String INVERT_NOTE_OCT_TAG = "invert_note_oct";
    public static final Byte FILTER_NOTE_OCT_ALL = -1;
    public static final String BROADCAST_NOTE_TAG = "broadcast_note";
    public static final String BROADCAST_PUBLIC_TAG = "broadcast_public";
    public static final String SOURCE_TAG = "source_uuid";
    public static final String SOURCE_NAME_TAG = "source_name";
    public static final String SYS_INPUT_TAG = "sys_input";
    public static final String ENABLED_CHANNELS_TAG = "channels";
    public static final String INSTRUMENT_TAG = "filter_instrument";
    public static final String INVERT_INSTRUMENT_TAG = "invert_instrument";
    public static final String VOLUME_TAG = "instrument_volume";
    public static final String INVERT_SIGNAL_TAG = "invert_signal";
    public static final String NOTE_START_TRIGGER_TAG = "note_start";
    public static final String HOLD_TICKS_TAG = "hold_ticks";
    public static final String TRANSMITTER_SOURCE_PREFIX = ";T;";
    public static final Byte INSTRUMENT_ALL = -1;
    public static final Byte MAX_TRIGGER_MODE = 3;
    public static final Byte MAX_HOLD_TICKS = 20;
    public static final Byte MAX_INSTRUMENT_VOLUME = 10;
    public static final Integer PERCUSSION_BANK = 120;
    public static final Byte DEFAULT_INSTRUMENT_VOLUME = 5;
    public static final Byte MIN_INSTRUMENT_VOLUME = 0;
    public static final Integer ALL_CHANNELS_INT = 65535;
    public static final Integer ALL_BUT_10_CHANNELS_INT = 65023;
    public static final Integer JUST_CHANNEL_10_INT = 512;
    public static final Integer NONE_CHANNELS_INT = 0;
    
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

    public static void setMidiSourceFromTransmitter(ItemStack stack,UUID sourceId, String sourceName) {
        setMidiSource(stack, sourceId, TRANSMITTER_SOURCE_PREFIX + sourceName);
    }

    public static void setMidiSource(ItemStack stack, UUID sourceId, String sourceName) {
        if (sourceId != null) {
            stack.getOrCreateTag().putUUID(SOURCE_TAG, sourceId);
        } else {
            stack.getOrCreateTag().remove(SOURCE_TAG);
        }

        if(sourceName != null) {
            stack.getOrCreateTag().putString(SOURCE_NAME_TAG, sourceName);
        } else {
            stack.getOrCreateTag().remove(SOURCE_NAME_TAG);
        }
    }

    public static UUID getMidiSource(ItemStack stack) {
        return TagUtils.getUUIDOrDefault(stack, SOURCE_TAG, null);
    }

    public static Boolean getMidiSourceIsTransmitter(ItemStack stack) {
        return TagUtils.getStringOrDefault(stack, SOURCE_NAME_TAG, "").startsWith(TRANSMITTER_SOURCE_PREFIX);
    }

    public static String getMidiSourceName(ItemStack stack, Boolean forDisplay) {
        UUID sourceId = getMidiSource(stack);

        if(sourceId == null) {
            return "None";
        }

        String name = TagUtils.getStringOrDefault(stack, SOURCE_NAME_TAG, "Unknown");
        return forDisplay ? name.replaceFirst(TRANSMITTER_SOURCE_PREFIX, "") : name;
    }

    public static void setEnabledChannelsInt(ItemStack stack, Integer enabledChannels) {
        if(enabledChannels != null) {
            stack.getOrCreateTag().putInt(ENABLED_CHANNELS_TAG, enabledChannels);
        }
    }

    public static Integer getEnabledChannelsInt(ItemStack stack) {
        return TagUtils.getIntOrDefault(stack, ENABLED_CHANNELS_TAG, getDefaultChannelsInt(stack));
    }

    public static Integer getDefaultChannelsInt(ItemStack stack) {
        if(stack.getItem() instanceof IInstrumentItem) {
            return ((IInstrumentItem)stack.getItem()).getDefaultChannels();
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
    
    public static SortedArraySet<Byte> getEnabledChannelsSet(ItemStack switchStack) {
        SortedArraySet<Byte> result = SortedArraySet.create(16);
        Integer enabledChannels = getEnabledChannelsInt(switchStack);

        for(byte i = 0; i < 16; i++) {
            if(((enabledChannels >> i) & 1) == 1) {
                result.add(i);
            }
        }

        return result;
    }

    public static void setFilterOct(ItemStack stack, Byte oct) {
        if (oct >= 0) {
            stack.getOrCreateTag().putByte(FILTER_OCT_TAG, oct);
        } else if (stack.hasTag()) {
            stack.getTag().remove(FILTER_OCT_TAG);
        }
    }
    
    public static void setFilterNote(ItemStack stack, Byte note) {
        if (note >= 0) {
            stack.getOrCreateTag().putByte(FILTER_NOTE_TAG, note);
        } else if (stack.hasTag()) {
            stack.getTag().remove(FILTER_NOTE_TAG);
        }
    }

    public static Byte getFilterOct(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, FILTER_OCT_TAG, FILTER_NOTE_OCT_ALL);
    }

    public static Byte getFilterNote(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, FILTER_NOTE_TAG, FILTER_NOTE_OCT_ALL);
    }

    public static void setBroadcastNote(ItemStack stack, Byte note) {
        if (note >= 0) {
            stack.getOrCreateTag().putByte(BROADCAST_NOTE_TAG, note);
        } else if (stack.hasTag()) {
            stack.getTag().remove(BROADCAST_NOTE_TAG);
        }
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
        if (instrumentId >= 0) {
            stack.getOrCreateTag().putByte(INSTRUMENT_TAG, instrumentId);
        } else if (stack.hasTag()) {
            stack.getTag().remove(INSTRUMENT_TAG);
        }
    }

    public static Byte getFilterInstrument(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, INSTRUMENT_TAG, INSTRUMENT_ALL);
    }
    
    public static void setSysInput(ItemStack stack, Boolean sysInput) {
        if (sysInput) {
            stack.getOrCreateTag().putBoolean(SYS_INPUT_TAG, sysInput);
        } else if (stack.hasTag()) {
            stack.getTag().remove(SYS_INPUT_TAG);
        }
    }

    public static Boolean getSysInput(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, SYS_INPUT_TAG, false);
    }
    
    public static void setInvertNoteOct(ItemStack stack, Boolean invert) {
        if (invert) {
            stack.getOrCreateTag().putBoolean(INVERT_NOTE_OCT_TAG, invert);
        } else if (stack.hasTag()) {
            stack.getTag().remove(INVERT_NOTE_OCT_TAG);
        }
    }

    public static Boolean getInvertNoteOct(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, INVERT_NOTE_OCT_TAG, false);
    }

    public static void setInvertInstrument(ItemStack stack, Boolean invert) {
        if (invert) {
            stack.getOrCreateTag().putBoolean(INVERT_INSTRUMENT_TAG, invert);
        } else if (stack.hasTag()) {
            stack.getTag().remove(INVERT_INSTRUMENT_TAG);
        }
    }

    public static Boolean getInvertInstrument(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, INVERT_INSTRUMENT_TAG, false);
    }

    public static Boolean getInvertSignal(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, INVERT_SIGNAL_TAG, false);
    }

    public static void setInvertSignal(ItemStack stack, Boolean invert) {
        if (invert) {
            stack.getOrCreateTag().putBoolean(INVERT_SIGNAL_TAG, invert);
        } else if (stack.hasTag()) {
            stack.getTag().remove(INVERT_SIGNAL_TAG);
        }
    }
    public static Boolean getTriggerNoteStart(ItemStack stack) {
        return TagUtils.getBooleanOrDefault(stack, NOTE_START_TRIGGER_TAG, false);
    }

    public static void setTriggerNoteStart(ItemStack stack, Boolean held) {
        if (held) {
            stack.getOrCreateTag().putBoolean(NOTE_START_TRIGGER_TAG, held);
        } else if (stack.hasTag()) {
            stack.getTag().remove(NOTE_START_TRIGGER_TAG);
        }
    }

    public static void setHoldTicks(ItemStack stack, Byte holdTicks) {
        if(holdTicks > MAX_HOLD_TICKS) {
            holdTicks = MAX_HOLD_TICKS;
        } else if(holdTicks < 1) {
            holdTicks = 1;
        }

        stack.getOrCreateTag().putByte(HOLD_TICKS_TAG, holdTicks);
    }

    public static Byte getHoldTicks(ItemStack stack) {
        Byte ticks = TagUtils.getByteOrDefault(stack, HOLD_TICKS_TAG, 1);
        return ticks < 1 ? 1 : ticks;
    }

    public static void setInstrumentVolume(ItemStack stack, Byte volume) {
        if(volume > MAX_INSTRUMENT_VOLUME) {
            volume = MAX_INSTRUMENT_VOLUME;
        } else if(volume < MIN_INSTRUMENT_VOLUME) {
            volume = MIN_INSTRUMENT_VOLUME;
        }

        stack.getOrCreateTag().putByte(VOLUME_TAG, volume);
    }

    public static Byte getInstrumentVolume(ItemStack stack) {
        return TagUtils.getByteOrDefault(stack, VOLUME_TAG, DEFAULT_INSTRUMENT_VOLUME);
    }

    public static Byte applyVolume(ItemStack stack, Byte sourceVelocity) {
        return applyVolume(getInstrumentVolume(stack), sourceVelocity);
    }

    public static Byte applyVolume(Byte volume, Byte sourceVelocity) {
        return Integer.valueOf(Double.valueOf((Double.valueOf(volume) / Double.valueOf(MAX_INSTRUMENT_VOLUME)) * sourceVelocity).intValue()).byteValue();
    }

    public static String getInstrumentName(Byte instrumentId) {
        return INSTRUMENT_NAME_MAP().get(instrumentId);
    }

    public static List<Byte> getFilterNotes(Byte note, Byte oct) {
        List<Byte> result = new ArrayList<>();

        if(oct != FILTER_NOTE_OCT_ALL && note != FILTER_NOTE_OCT_ALL && Integer.valueOf(oct*12+note) <= Byte.MAX_VALUE) {
            result.add(Integer.valueOf(oct*12+note).byteValue());
        } else if(oct != FILTER_NOTE_OCT_ALL) {
            for(int i = 0; i < 12; i++) {
                if(Integer.valueOf(oct*12+i) <= Byte.MAX_VALUE) {
                    result.add(Integer.valueOf(oct*12+i).byteValue());
                }
            }
        } else if(note != FILTER_NOTE_OCT_ALL) {
            for(int i = 0; i < 10; i++) {
                if(Integer.valueOf(i*12+note) <= Byte.MAX_VALUE) {
                    result.add(Integer.valueOf(i*12+note).byteValue());
                }
            }
        }

        return result;
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

    public static String getBroadcastNoteAsString(ItemStack stack) {
        return getMidiNoteAsString(getBroadcastNote(stack));
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

    public static Boolean isInstrumentFiltered(ItemStack stack, Byte instrument) {
        Byte filterInstrument = getFilterInstrument(stack);
        return filterInstrument.equals(INSTRUMENT_ALL) ? !getInvertInstrument(stack) : getInvertInstrument(stack) ? filterInstrument != instrument : filterInstrument == instrument;
    }

    public static Boolean isInstrumentFiltered(Byte filterInstrument, Boolean invertInstrument, Byte instrument) {
        return filterInstrument.equals(INSTRUMENT_ALL) ? !invertInstrument : invertInstrument ? filterInstrument != instrument : filterInstrument == instrument;
    }

    public static CompoundTag convertSwitchboardToDataTag(CompoundTag switchTag) {
        CompoundTag instrumentTag = switchTag != null ? switchTag.copy() : new CompoundTag();

        // OLD DEFAULT CHANNEL --> NONE
        if(!instrumentTag.contains("enabled_channels") || instrumentTag.getString("enabled_channels").isBlank()) {
            instrumentTag.putInt(ENABLED_CHANNELS_TAG, NONE_CHANNELS_INT);
        }
        
        if(instrumentTag.contains("enabled_channels")) {
            instrumentTag.remove("enabled_channels");
        }

        // OLD DEFAULT SOURCE NAME --> NONE
        if(!instrumentTag.contains(SOURCE_NAME_TAG)|| instrumentTag.getString(SOURCE_NAME_TAG).isBlank()) {
            instrumentTag.putString(SOURCE_NAME_TAG, "None");
        }

        return instrumentTag;
    }

    public static Boolean shouldInstrumentRespondToMessage(ItemStack stack, UUID sender, Byte channel) {
        return stack.getItem() instanceof IInstrumentItem && MidiNbtDataUtils.isChannelEnabled(stack, channel) && 
               (sender != null && sender.equals(MidiNbtDataUtils.getMidiSource(stack)));
    }

    public static void appendSettingsTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        Integer enabledChannels = getEnabledChannelsInt(stack);
        if(enabledChannels != null) {
            if(enabledChannels.equals(ALL_CHANNELS_INT)) {
                tooltip.add(Component.literal("  Channels: All").withStyle(ChatFormatting.GREEN));
            } else if(enabledChannels.equals(NONE_CHANNELS_INT)) {
                tooltip.add(Component.literal("  Channels: None").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("  Channels: " + getEnabledChannelsAsString(enabledChannels)).withStyle(ChatFormatting.GREEN));
            }
        }

        // Note Source
        if(getMidiSource(stack) != null) {
            tooltip.add(Component.literal("  Play Notes From: " + (getMidiSourceIsTransmitter(stack) ? "Transmitter" : "Player")).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  " + getMidiSourceName(stack, true)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("  Play Notes From: None").withStyle(ChatFormatting.GREEN));
        }

        // Instrument Volume
        tooltip.add(Component.literal("  Volume: " + getInstrumentVolume(stack)).withStyle(ChatFormatting.GREEN));

        // MIDI Device Input
        if(getSysInput(stack)) {
            tooltip.add(Component.literal("  Device Input: Enabled").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("  Device Input: Disabled").withStyle(ChatFormatting.GREEN));
        }
    }
}
