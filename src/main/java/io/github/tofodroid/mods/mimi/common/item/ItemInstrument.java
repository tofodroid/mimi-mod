package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.midi.MidiChannelDef.MidiChannelNumber;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.instruments.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.network.InstrumentItemDataUpdatePacket;
import io.github.tofodroid.mods.mimi.common.instruments.InstrumentDataUtil;

public class ItemInstrument extends Item {
    private final Byte instrumentId;

    public ItemInstrument(String name, final Byte instrumentId) {
        super(new Properties().group(ModItems.ITEM_GROUP).maxStackSize(1));
        this.setRegistryName(name);
        this.instrumentId = instrumentId;
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        // Server-side only
        if(!playerIn.getEntityWorld().isRemote()) {  
            if(target instanceof PlayerEntity) {
                ItemInstrumentDataUtil.INSTANCE.linkToMaestro(stack, target.getUniqueID());
                playerIn.setHeldItem(hand, stack);
                playerIn.sendStatusMessage(new StringTextComponent("Linked Maestro: " +  target.getName().getString()), true);
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
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        final ItemStack heldItem = playerIn.getHeldItem(handIn);
        if(worldIn.isRemote) {
            // Regular right click - Open GUI, Sneak right click - cycle input mode
            if(!playerIn.isSneaking()) {
                MIMIMod.guiWrapper.openInstrumentGui(worldIn, playerIn, instrumentId, heldItem);
                return new ActionResult<>(ActionResultType.SUCCESS, heldItem);
            }
        } else {
            if(playerIn.isSneaking()) {
                ItemInstrumentDataUtil.INSTANCE.toggleMidiEnabled(heldItem);
                Boolean enabled = ItemInstrumentDataUtil.INSTANCE.isMidiEnabled(heldItem);
                playerIn.sendStatusMessage(new StringTextComponent(enabled ? "MIDI Input Enabled" : "MIDI Input Disabled"), true);
                return new ActionResult<>(ActionResultType.SUCCESS, heldItem);
            }
        }
        
        return new ActionResult<>(ActionResultType.PASS, heldItem);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        // Client-side only
        if(worldIn != null && worldIn.isRemote) {
            SortedArraySet<Byte> acceptedChannels = ItemInstrumentDataUtil.INSTANCE.getAcceptedChannelsSet(stack);
            
            tooltip.add(new StringTextComponent("----------------"));

            Boolean enabled = ItemInstrumentDataUtil.INSTANCE.isMidiEnabled(stack);
            tooltip.add(new StringTextComponent(enabled ? "MIDI Input Enabled" : "MIDI Input Disabled"));

            // Linked Maestro
            UUID maestroId = ItemInstrumentDataUtil.INSTANCE.getLinkedMaestro(stack);
            if(InstrumentDataUtil.MIDI_MAESTRO_ID.equals(maestroId)) {
                tooltip.add(new StringTextComponent("Linked to System MIDI Device"));
            } else if(InstrumentDataUtil.PUBLIC_MAESTRO_ID.equals(maestroId)) {
                tooltip.add(new StringTextComponent("Linked to Public Transmitters"));
            } else if(maestroId != null) {
                tooltip.add(new StringTextComponent("Linked to Player Transmitter"));
            }
    
            // Listen Channels
            if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
                if(acceptedChannels.size() == MidiChannelNumber.values().length) {
                    tooltip.add(new StringTextComponent("Enabled Channels: All"));
                } else {
                    tooltip.add(new StringTextComponent("Enabled Channels: " + acceptedChannels.stream().map(c -> new Integer(c.intValue()+1).toString()).collect(Collectors.joining(", "))));
                }
            } else {
                tooltip.add(new StringTextComponent("Enabled Channels: None"));
            }
        }
    }

    public Byte getInstrumentId() {
        return this.instrumentId;
    }

    // Data Utils    
    public static ItemStack getEntityHeldInstrumentStack(LivingEntity entity, Hand handIn) {
        ItemStack heldStack = entity.getHeldItem(handIn);

        if(heldStack != null && heldStack.getItem() instanceof ItemInstrument) {
            return heldStack;
        }

        return null;
    }
    
    public static Boolean isEntityHoldingInstrument(LivingEntity entity) {
        return getEntityHeldInstrumentStack(entity, Hand.MAIN_HAND) != null 
            || getEntityHeldInstrumentStack(entity, Hand.OFF_HAND) != null;
    }

    public static Byte getEntityHeldInstrumentId(LivingEntity entity, Hand handIn) {
        ItemStack instrumentStack = getEntityHeldInstrumentStack(entity, handIn);

        if(instrumentStack != null) {
            return ((ItemInstrument)instrumentStack.getItem()).getInstrumentId();
        }

        return null;
    }

    public static InstrumentItemDataUpdatePacket getSyncPacket(ItemStack stack) {
        return new InstrumentItemDataUpdatePacket(
            ItemInstrumentDataUtil.INSTANCE.getLinkedMaestro(stack), 
            ItemInstrumentDataUtil.INSTANCE.isMidiEnabled(stack), 
            ItemInstrumentDataUtil.INSTANCE.getAcceptedChannelsString(stack)
        );
    }
}
