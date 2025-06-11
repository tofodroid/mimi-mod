package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IInstrumentItem {
    public Byte getInstrumentId();
    public Integer getDefaultChannels();
    public String getRegistryName();
    public Integer getDefaultColor();

    default public void appendSettingsTooltip(ItemStack stack, List<Component> tooltip) {
        IInstrumentItem.appendInstrumentTooltip(stack, tooltip);
    }

    public static void appendInstrumentTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        MidiNbtDataUtils.appendEnabledChannelsTooltip(stack, tooltip);
        MidiNbtDataUtils.appendMidiSourceTooltip(stack, tooltip);
        MidiNbtDataUtils.appendInstrumentVolumeTooltip(stack, tooltip);
        MidiNbtDataUtils.appendDeviceInputEnabledTooltip(stack, tooltip);
    }
}
