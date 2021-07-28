package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.midi.MidiChannelDef.MidiChannelNumber;
import io.github.tofodroid.mods.mimi.common.network.SwitchboardStackUpdatePacket;

public class ItemMidiSwitchboard extends Item {
    public static final String FILTER_NOTE_TAG = "filter_note";
    public static final String SOURCE_TAG = "source_uuid";
    public static final String ENABLED_CHANNELS_TAG = "enabled_channels";
    public static final UUID NONE_SOURCE_ID = new UUID(0,0);
    public static final UUID SYS_SOURCE_ID = new UUID(0,1);
    public static final UUID PUBLIC_SOURCE_ID = new UUID(0,2);
    public static final String ALL_CHANNELS_STRING = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16";

    public ItemMidiSwitchboard() {
        super(new Properties().group(ModItems.ITEM_GROUP).maxStackSize(1));
        this.setRegistryName("switchboard");
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        // Server-side only
        if(!playerIn.getEntityWorld().isRemote()) {  
            if(target instanceof PlayerEntity) {
                ItemMidiSwitchboard.setMidiSource(stack, target.getUniqueID());
                playerIn.setHeldItem(hand, stack);
                playerIn.sendStatusMessage(new StringTextComponent("Set MIDI Source to: " +  target.getName().getString()), true);
                return ActionResultType.CONSUME;
            }
        } else {
            // Cancel click event client side
            if(target instanceof PlayerEntity) {
                return ActionResultType.SUCCESS;
            }
        }
        
        return ActionResultType.PASS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        // Client-side only
        if(worldIn != null && worldIn.isRemote) {
            SortedArraySet<Byte> acceptedChannels = ItemMidiSwitchboard.getEnabledChannelsSet(stack);
            
            tooltip.add(new StringTextComponent("----------------"));

            // MIDI Source Filter
            UUID maestroId = ItemMidiSwitchboard.getMidiSource(stack);
            if(SYS_SOURCE_ID.equals(maestroId)) {
                tooltip.add(new StringTextComponent("Linked to System MIDI Device"));
            } else if(PUBLIC_SOURCE_ID.equals(maestroId)) {
                tooltip.add(new StringTextComponent("Linked to Public Transmitters"));
            } else if(maestroId != null) {
                tooltip.add(new StringTextComponent("Linked to Player Transmitter"));
            }

            // MIDI Source Filter
            if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
                if(acceptedChannels.size() == MidiChannelNumber.values().length) {
                    tooltip.add(new StringTextComponent("Enabled MIDI Channels: All"));
                } else {
                    tooltip.add(new StringTextComponent("Enabled MIDI Channels: " + acceptedChannels.stream().map(c -> new Integer(c.intValue()+1).toString()).collect(Collectors.joining(", "))));
                }
            } else {
                tooltip.add(new StringTextComponent("Enabled MIDI Channels: None"));
            }

            // MIDI Note Filter
            // TODO
        }
    }

    // DATA FUNCTIONS
    public static void setMidiSource(ItemStack stack, UUID sourceId) {
        if (sourceId != null) {
            stack.getOrCreateTag().putUniqueId(SOURCE_TAG, sourceId);
        } else if (stack.hasTag()) {
            stack.getTag().remove(SOURCE_TAG);
        }
    }

    public static UUID getMidiSource(ItemStack stack) {
        if (stackTagContainsKey(stack, SOURCE_TAG)) {
            return stack.getTag().getUniqueId(SOURCE_TAG);
        }

        return null;
    }

    public static void setEnabledChannelsString(ItemStack stack, String acceptedChannelsString) {
        if (acceptedChannelsString != null && !acceptedChannelsString.trim().isEmpty()) {
            stack.getOrCreateTag().putString(ENABLED_CHANNELS_TAG, acceptedChannelsString);
        } else if (stack.hasTag()) {
            stack.getTag().remove(ENABLED_CHANNELS_TAG);
        }
    }

    public static String getEnabledChannelsString(ItemStack stack) {
        if (stackTagContainsKey(stack, ENABLED_CHANNELS_TAG)) {
            return stack.getTag().getString(ENABLED_CHANNELS_TAG);
        }

        return null;
    }

    public static void toggleChannel(ItemStack switchStack, Byte channelId) {
        if(channelId != null && channelId < 16 && channelId >= 0) {
            SortedArraySet<Byte> acceptedChannels = getEnabledChannelsSet(switchStack);

            if(acceptedChannels == null) {
                acceptedChannels = SortedArraySet.newSet(16);
            }

            if(isChannelEnabled(switchStack, channelId)) {
                acceptedChannels.remove(channelId);
            } else {
                acceptedChannels.add(channelId);
            }

            String acceptedChannelsString = acceptedChannels.stream().map(b -> new Integer(b + 1).toString()).collect(Collectors.joining(","));
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
        return getEnabledChannelsSet(switchStack).contains(channelId);
    }
    
    public static SortedArraySet<Byte> getEnabledChannelsSet(ItemStack switchStack) {
        String acceptedChannelString = getEnabledChannelsString(switchStack);

        if(acceptedChannelString != null && !acceptedChannelString.isEmpty()) {
            SortedArraySet<Byte> result = SortedArraySet.newSet(16);
            result.addAll(Arrays.asList(acceptedChannelString.split(",", -1)).stream().map(b -> new Integer(Byte.valueOf(b) - 1).byteValue()).collect(Collectors.toSet()));
            return result;
        }

        return SortedArraySet.newSet(0);
    }

    public static void setFilterNoteString(ItemStack stack, String filterString) {
        if (filterString != null && !filterString.trim().isEmpty()) {
            stack.getOrCreateTag().putString(FILTER_NOTE_TAG, filterString);
        } else if (stack.hasTag()) {
            stack.getTag().remove(FILTER_NOTE_TAG);
        }
    }

    public static String getFilterNoteString(ItemStack stack) {
        if (stackTagContainsKey(stack, FILTER_NOTE_TAG)) {
            return stack.getTag().getString(FILTER_NOTE_TAG);
        }

        return null;
    }
    
    public static ArrayList<Byte> getFilterNotes(ItemStack stack) {
        String filterString = getFilterNoteString(stack);

        if(filterString != null && !filterString.isEmpty()) {
            ArrayList<Byte> result = new ArrayList<>();
            result.addAll(Arrays.asList(filterString.split(",", -1)).stream().map(b -> new Integer(Byte.valueOf(b)).byteValue()).collect(Collectors.toSet()));
            return result;
        }

        return new ArrayList<>();
    }

    public static Boolean isNoteFiltered(ItemStack stack, Byte note) {
        return getFilterNotes(stack).contains(note);
    }

    protected static Boolean stackTagContainsKey(ItemStack stack, String tag) {
        return stack != null && stack.getTag() != null && stack.getTag().contains(tag);
    }

    public static SwitchboardStackUpdatePacket getSyncPacket(ItemStack stack) {
        return new SwitchboardStackUpdatePacket(
            ItemMidiSwitchboard.getMidiSource(stack),
            ItemMidiSwitchboard.getEnabledChannelsString(stack),
            ItemMidiSwitchboard.getFilterNoteString(stack)
        );
    }
}
