package io.github.tofodroid.mods.mimi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("null")
public abstract class InstrumentDataUtils {
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
    public static final String ENABLED_CHANNELS_TAG = "enabled_channels";
    public static final String INSTRUMENT_TAG = "filter_instrument";
    public static final String INVERT_INSTRUMENT_TAG = "invert_instrument";
    public static final String VOLUME_TAG = "instrument_volume";
    public static final Byte INSTRUMENT_ALL = -1;
    public static final Byte MAX_INSTRUMENT_VOLUME = 5;
    public static final Integer PERCUSSION_BANK = 120;
    public static final Byte DEFAULT_INSTRUMENT_VOLUME = 3;
    public static final Byte MIN_INSTRUMENT_VOLUME = 0;

    public static final UUID PUBLIC_SOURCE_ID = new UUID(0,2);
    public static final UUID NONE_SOURCE_ID = new UUID(0,0);
    public static final String ALL_CHANNELS_STRING = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16";
    public static final String NONE_CHANNELS_STRING = "NONE";
    
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

    public static void setMidiSource(ItemStack stack, UUID sourceId, String sourceName) {
        if (sourceId != null) {
            stack.getOrCreateTag().putUUID(SOURCE_TAG, sourceId);
        } else {
            stack.getOrCreateTag().putUUID(SOURCE_TAG, NONE_SOURCE_ID);
        }

        if(sourceName != null) {
            stack.getOrCreateTag().putString(SOURCE_NAME_TAG, sourceName);
        }
    }

    public static UUID getMidiSource(ItemStack stack) {
        if (stackTagContainsKey(stack, SOURCE_TAG)) {
            return stack.getTag().getUUID(SOURCE_TAG);
        }

        return PUBLIC_SOURCE_ID;
    }

    public static String getMidiSourceName(ItemStack stack) {
        UUID sourceId = getMidiSource(stack);

        if(stackTagContainsKey(stack, SOURCE_NAME_TAG)) {
            return stack.getTag().getString(SOURCE_NAME_TAG);
        } else if(sourceId.equals(PUBLIC_SOURCE_ID)) {
            return "Public";
        } else if(sourceId.equals(NONE_SOURCE_ID)) {
            return "None";
        }

        return "Unknown";
    }

    public static void setEnabledChannelsString(ItemStack stack, String enabledChannelsString) {
        if (enabledChannelsString != null && !enabledChannelsString.trim().isEmpty()) {
            stack.getOrCreateTag().putString(ENABLED_CHANNELS_TAG, enabledChannelsString);
        } else {
            stack.getOrCreateTag().putString(ENABLED_CHANNELS_TAG, NONE_CHANNELS_STRING);
        }
    }

    public static String getEnabledChannelsString(ItemStack stack) {
        if (!stackTagContainsKey(stack, ENABLED_CHANNELS_TAG) || stack.getTag().getString(ENABLED_CHANNELS_TAG).isEmpty()) {
            setEnabledChannelsString(stack, getDefaultChannels(stack));
        }

        return stack.getTag().getString(ENABLED_CHANNELS_TAG);
    }

    public static void toggleChannel(ItemStack switchStack, Byte channelId) {
        if(channelId != null && channelId < 16 && channelId >= 0) {
            SortedArraySet<Byte> acceptedChannels = getEnabledChannelsSet(switchStack);

            if(acceptedChannels == null) {
                acceptedChannels = SortedArraySet.create(16);
            }

            if(acceptedChannels.contains(channelId)) {
                acceptedChannels.remove(channelId);
            } else {
                acceptedChannels.add(channelId);
            }

            String acceptedChannelsString = acceptedChannels.stream().map(b -> Integer.valueOf(b + 1).toString()).collect(Collectors.joining(","));
            setEnabledChannelsString(switchStack, acceptedChannelsString);
        }
    }
    
    public static void clearEnabledChannels(ItemStack switchStack) {
        setEnabledChannelsString(switchStack, null);
    }
    
    public static void setEnableAllChannels(ItemStack switchStack) {
        setEnabledChannelsString(switchStack, ALL_CHANNELS_STRING);
    }

    public static Boolean isChannelEnabled(ItemStack switchStack, Byte channelId) {
        if(ALL_CHANNELS.equals(channelId)) {
            return true;
        }

        String enabledChannels = getEnabledChannelsString(switchStack);
        return !NONE_CHANNELS_STRING.equals(enabledChannels) && Arrays.asList(enabledChannels.split(",", -1)).contains(String.valueOf((channelId+1)));
    }
    
    public static SortedArraySet<Byte> getEnabledChannelsSet(ItemStack switchStack) {
        String acceptedChannelString = getEnabledChannelsString(switchStack);

        if(!acceptedChannelString.isBlank() && !NONE_CHANNELS_STRING.equals(acceptedChannelString)) {
            SortedArraySet<Byte> result = SortedArraySet.create(16);
            result.addAll(Arrays.asList(acceptedChannelString.split(",", -1)).stream().map(b -> Integer.valueOf(Byte.valueOf(b) - 1).byteValue()).collect(Collectors.toSet()));
            return result;
        }

        return SortedArraySet.create(0);
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
        if (stackTagContainsKey(stack, FILTER_OCT_TAG)) {
            return stack.getTag().getByte(FILTER_OCT_TAG);
        }

        return FILTER_NOTE_OCT_ALL;
    }

    public static Byte getFilterNote(ItemStack stack) {
        if (stackTagContainsKey(stack, FILTER_NOTE_TAG)) {
            return stack.getTag().getByte(FILTER_NOTE_TAG);
        }

        return FILTER_NOTE_OCT_ALL;
    }

    public static void setBroadcastNote(ItemStack stack, Byte note) {
        if (note >= 0) {
            stack.getOrCreateTag().putByte(BROADCAST_NOTE_TAG, note);
        } else if (stack.hasTag()) {
            stack.getTag().remove(BROADCAST_NOTE_TAG);
        }
    }

    public static Byte getBroadcastNote(ItemStack stack) {
        if (stackTagContainsKey(stack, BROADCAST_NOTE_TAG)) {
            return stack.getTag().getByte(BROADCAST_NOTE_TAG);
        }

        return 0;
    }
    
    public static void setPublicBroadcast(ItemStack stack, Boolean pub) {
        if (pub) {
            stack.getOrCreateTag().putBoolean(BROADCAST_PUBLIC_TAG, pub);
        } else if (stack.hasTag()) {
            stack.getTag().remove(BROADCAST_PUBLIC_TAG);
        }
    }

    public static Boolean getPublicBroadcast(ItemStack stack) {
        if (stackTagContainsKey(stack, BROADCAST_PUBLIC_TAG)) {
            return stack.getTag().getBoolean(BROADCAST_PUBLIC_TAG);
        }

        return false;
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
        if (stackTagContainsKey(stack, INSTRUMENT_TAG)) {
            return stack.getTag().getByte(INSTRUMENT_TAG);
        }

        return INSTRUMENT_ALL;
    }
    
    public static void setSysInput(ItemStack stack, Boolean sysInput) {
        if (sysInput) {
            stack.getOrCreateTag().putBoolean(SYS_INPUT_TAG, sysInput);
        } else if (stack.hasTag()) {
            stack.getTag().remove(SYS_INPUT_TAG);
        }
    }

    public static Boolean getSysInput(ItemStack stack) {
        if (stackTagContainsKey(stack, SYS_INPUT_TAG)) {
            return stack.getTag().getBoolean(SYS_INPUT_TAG);
        }

        return false;
    }
    
    public static void setInvertNoteOct(ItemStack stack, Boolean invert) {
        if (invert) {
            stack.getOrCreateTag().putBoolean(INVERT_NOTE_OCT_TAG, invert);
        } else if (stack.hasTag()) {
            stack.getTag().remove(INVERT_NOTE_OCT_TAG);
        }
    }

    public static Boolean getInvertNoteOct(ItemStack stack) {
        if (stackTagContainsKey(stack, INVERT_NOTE_OCT_TAG)) {
            return stack.getTag().getBoolean(INVERT_NOTE_OCT_TAG);
        }

        return false;
    }

    public static void setInvertInstrument(ItemStack stack, Boolean invert) {
        if (invert) {
            stack.getOrCreateTag().putBoolean(INVERT_INSTRUMENT_TAG, invert);
        } else if (stack.hasTag()) {
            stack.getTag().remove(INVERT_INSTRUMENT_TAG);
        }
    }

    public static Boolean getInvertInstrument(ItemStack stack) {
        if (stackTagContainsKey(stack, INVERT_INSTRUMENT_TAG)) {
            return stack.getTag().getBoolean(INVERT_INSTRUMENT_TAG);
        }

        return false;
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
        if (stackTagContainsKey(stack, VOLUME_TAG)) {
            return stack.getTag().getByte(VOLUME_TAG);
        }

        return DEFAULT_INSTRUMENT_VOLUME;
    }

    public static String getInstrumentVolumePercent(ItemStack stack) {
        Integer value = Integer.valueOf(Double.valueOf((Double.valueOf(getInstrumentVolume(stack)) / Double.valueOf(MAX_INSTRUMENT_VOLUME)) * 10).intValue());
        return value == 10 ? value.toString() : "0" + value.toString();
    }

    public static Byte applyVolume(ItemStack stack, Byte sourceVelocity) {
        return Integer.valueOf(Double.valueOf((Double.valueOf(getInstrumentVolume(stack)) / Double.valueOf(MAX_INSTRUMENT_VOLUME)) * sourceVelocity).intValue()).byteValue();
    }

    public static String getInstrumentName(Byte instrumentId) {
        return INSTRUMENT_NAME_MAP().get(instrumentId);
    }

    public static List<Byte> getFilterNotes(ItemStack stack) {
        List<Byte> result = new ArrayList<>();
        Byte oct = getFilterOct(stack);
        Byte note = getFilterNote(stack);

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

    public static String getDefaultChannels(ItemStack stack) {
        if(stack.getItem() instanceof IInstrumentItem) {
            return ((IInstrumentItem)stack.getItem()).getDefaultChannels();
        } else {
            return NONE_CHANNELS_STRING;
        }
    }

    public static String getDefaultChannelsForBank(Integer bankNumber) {
        if(bankNumber == PERCUSSION_BANK) {
            return "10";
        }
        return "1,2,3,4,5,6,7,8,9,11,12,13,14,15,16";
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

    public static Boolean isNoteFiltered(ItemStack stack, Byte note) {
        List<Byte> filteredNotes = getFilterNotes(stack);
        return filteredNotes.isEmpty() ? !getInvertNoteOct(stack) : getInvertNoteOct(stack) ? !filteredNotes.contains(note) : filteredNotes.contains(note);
    }

    public static Boolean isInstrumentFiltered(ItemStack stack, Byte instrument) {
        Byte filterInstrument = getFilterInstrument(stack);
        return filterInstrument.equals(INSTRUMENT_ALL) ? !getInvertInstrument(stack) : getInvertInstrument(stack) ? filterInstrument != instrument : filterInstrument == instrument;
    }

    protected static Boolean stackTagContainsKey(ItemStack stack, String tag) {
        return stack != null && stack.getTag() != null && stack.getTag().contains(tag);
    }

    public static CompoundTag convertSwitchboardToInstrumentTag(CompoundTag switchTag) {
        CompoundTag instrumentTag = switchTag != null ? switchTag.copy() : new CompoundTag();

        // OLD DEFAULT CHANNEL --> NONE
        if(!instrumentTag.contains(ENABLED_CHANNELS_TAG) || instrumentTag.getString(ENABLED_CHANNELS_TAG).isBlank()) {
            instrumentTag.putString(ENABLED_CHANNELS_TAG, NONE_CHANNELS_STRING);
        }

        // OLD DEFAULT SOURCE ID --> NONE
        if(!instrumentTag.contains(SOURCE_TAG) || instrumentTag.getUUID(SOURCE_TAG) == null) {
            instrumentTag.putUUID(SOURCE_TAG, NONE_SOURCE_ID);
        }

        // OLD DEFAULT SOURCE NAME --> NONE
        if(!instrumentTag.contains(SOURCE_NAME_TAG)|| instrumentTag.getString(SOURCE_NAME_TAG).isBlank()) {
            instrumentTag.putString(SOURCE_NAME_TAG, "None");
        }

        return instrumentTag;
    }
    
    public static Boolean shouldInstrumentRespondToMessage(ItemStack stack, UUID sender, Byte channel) {
        return stack.getItem() instanceof IInstrumentItem && InstrumentDataUtils.isChannelEnabled(stack, channel) && 
               (sender != null && sender.equals(InstrumentDataUtils.getMidiSource(stack)));
    }

    public static void appendMidiSettingsTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        String enabledChannels = getEnabledChannelsString(stack);
        if(enabledChannels != null && !enabledChannels.isEmpty()) {
            if(enabledChannels.equals(ALL_CHANNELS_STRING)) {
                tooltip.add(Component.literal("  Channels: All").withStyle(ChatFormatting.GREEN));
            } else if(enabledChannels.equals(NONE_CHANNELS_STRING)) {
                tooltip.add(Component.literal("  Channels: None").withStyle(ChatFormatting.GREEN));
            } else if(enabledChannels.equals(getDefaultChannels(stack))) {
                tooltip.add(Component.literal("  Channels: Default").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("  Channels: " + enabledChannels).withStyle(ChatFormatting.GREEN));
            }
        }

        // Note Source
        tooltip.add(Component.literal("  Play Notes From: " + getMidiSourceName(stack)).withStyle(ChatFormatting.GREEN));

        // Instrument Volume
        tooltip.add(Component.literal("  Volume: " + getInstrumentVolumePercent(stack)).withStyle(ChatFormatting.GREEN));

        // MIDI Device Input
        if(getSysInput(stack)) {
            tooltip.add(Component.literal("  Device Input: Enabled").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("  Device Input: Disabled").withStyle(ChatFormatting.GREEN));
        }
    }
}
