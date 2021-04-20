package io.github.tofodroid.mods.mimi.common.instruments;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;

import net.minecraft.item.ItemStack;

public class ItemInstrumentDataUtil extends InstrumentDataUtil<ItemStack> {
    public static final ItemInstrumentDataUtil INSTANCE = new ItemInstrumentDataUtil();

    @Override
    public void linkToMaestro(ItemStack stack, UUID playerId) {
        if (playerId != null) {
            stack.getOrCreateTag().putUniqueId(MAESTRO_TAG, playerId);
        } else if (stack.hasTag()) {
            stack.getTag().remove(MAESTRO_TAG);
        }
    }

    @Override
    public UUID getLinkedMaestro(ItemStack stack) {
        if (stackTagContainsKey(stack, MAESTRO_TAG)) {
            return stack.getTag().getUniqueId(MAESTRO_TAG);
        }

        return null;
    }

    @Override
    protected void setAcceptedChannelString(ItemStack stack, String acceptedChannelsString) {
        if (acceptedChannelsString != null && !acceptedChannelsString.trim().isEmpty()) {
            stack.getOrCreateTag().putString(LISTEN_CHANNELS_TAG, acceptedChannelsString);
        } else if (stack.hasTag()) {
            stack.getTag().remove(LISTEN_CHANNELS_TAG);
        }
    }

    @Override
    public String getAcceptedChannelsString(ItemStack stack) {
        if (stackTagContainsKey(stack, LISTEN_CHANNELS_TAG)) {
            return stack.getTag().getString(LISTEN_CHANNELS_TAG);
        }

        return null;
    }

    @Override
    public Byte getInstrumentIdFromData(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemInstrument) {
            return ((ItemInstrument) stack.getItem()).getInstrumentId();
        }
        return null;
    }

    @Override
    protected void setMidiEnabled(ItemStack stack, Boolean enabled) {
        if(enabled) {
            stack.getOrCreateTag().putBoolean(MIDI_ENABLED_TAG, enabled);
        } else if(stack.hasTag()) {
            stack.getTag().remove(MIDI_ENABLED_TAG);
        }
    }

    @Override
    public Boolean isMidiEnabled(ItemStack stack) {
        return stackTagContainsKey(stack, MIDI_ENABLED_TAG);
    }

    protected Boolean stackTagContainsKey(ItemStack stack, String tag) {
        return stack != null && stack.getTag() != null && stack.getTag().contains(tag);
    }
}
