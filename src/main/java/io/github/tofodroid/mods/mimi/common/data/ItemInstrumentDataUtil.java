package io.github.tofodroid.mods.mimi.common.data;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;

import net.minecraft.item.ItemStack;

public class ItemInstrumentDataUtil extends InstrumentDataUtil<ItemStack> {
    public static final ItemInstrumentDataUtil INSTANCE = new ItemInstrumentDataUtil();

    @Override
    public void setMidiSource(ItemStack stack, UUID sourceId) {
        if (sourceId != null) {
            stack.getOrCreateTag().putUniqueId(SOURCE_TAG, sourceId);
        } else if (stack.hasTag()) {
            stack.getTag().remove(MAESTRO_TAG);
            stack.getTag().remove(SOURCE_TAG);
        }
    }

    @Override
    public UUID getMidiSource(ItemStack stack) {
        if (stackTagContainsKey(stack, SOURCE_TAG)) {
            return stack.getTag().getUniqueId(SOURCE_TAG);
        } else if(stackTagContainsKey(stack, MAESTRO_TAG)) {
            setMidiSource(stack, stack.getTag().getUniqueId(MAESTRO_TAG));
            return stack.getTag().getUniqueId(SOURCE_TAG);
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
    public String getInstrumentName(ItemStack stcak) {
        return stcak.getItem().getName().getString();
    }

    protected Boolean stackTagContainsKey(ItemStack stack, String tag) {
        return stack != null && stack.getTag() != null && stack.getTag().contains(tag);
    }
}
