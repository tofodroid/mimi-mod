package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.codehaus.plexus.util.StringUtils;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerTransmitter;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class ItemTransmitter extends Item {
    public static final String REGISTRY_NAME = "transmitter";
    public static final String TRANSMIT_MODE_TAG = "broadcastMode";
    public static final String TRANSMIT_ID_TAG = "transmit_id";
    public static final String INVENTORY_TAG = "inventory";

    public ItemTransmitter() {
        super(new Properties().tab(ModItems.ITEM_GROUP).stacksTo(1));
    }
    
    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!worldIn.isClientSide && worldIn instanceof ServerLevel) {
            Integer invSlot = InteractionHand.MAIN_HAND.equals(handIn) ? playerIn.getInventory().selected 
            : Inventory.SLOT_OFFHAND;

            NetworkHooks.openScreen((ServerPlayer) playerIn, generateContainerProvider(
                invSlot
            ), buffer -> {
                buffer.writeInt(invSlot);
            });
            return new InteractionResultHolder<>(InteractionResult.CONSUME, playerIn.getItemInHand(handIn));
        }

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // Client-side only
        if(worldIn != null && worldIn.isClientSide) {
            
            tooltip.add(Component.literal("----------------"));

            if(stack.hasFoil()) {
                tooltip.add(Component.literal("§2§lCurrently Playing§r"));
                tooltip.add(Component.literal("§oMoving this Transmitter will stop playing§r"));
                tooltip.add(Component.literal("----------------"));
            }

            // Disk Title
            if(ItemTransmitter.hasActiveFloppyDisk(stack)) {
                tooltip.add(Component.literal("Loaded Disk: " + ItemFloppyDisk.getDiskTitle(ItemTransmitter.getActiveFloppyDiskStack(stack))));
            }

            // Transmit Mode
            tooltip.add(Component.literal("Transmit Mode: " + StringUtils.capitalizeFirstLetter(ItemTransmitter.getTransmitMode(stack).name().toLowerCase())));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ItemTransmitter.getTransmitId(stack) != null && ItemTransmitter.getTransmitId(stack).equals(((ClientProxy)MIMIMod.proxy).getMidiInput().getActiveTransmitterIdCache());
    }
    
    public static ItemStackHandler getInventoryHandler(ItemStack stack) {
		ItemStackHandler handler = new ItemStackHandler(1);

		if(stack == null || stack.isEmpty()) {
            return null;
        }

		if(stack.hasTag() && stack.getTag().contains(INVENTORY_TAG)) {
            handler.deserializeNBT(stack.getTag().getCompound(INVENTORY_TAG));
        }

		return handler;
	}

    public MenuProvider generateContainerProvider(Integer playerInventorySlot) {
        return new TransmitterMenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int p1, Inventory p2,  Player p3) {
                return new ContainerTransmitter(p1, p2, playerInventorySlot);
            }
    
            @Override
            public Component getDisplayName() {return Component.literal("mimi.transmitter");}
        };
    }
    
    public static ItemStack getActiveFloppyDiskStack(ItemStack stack) {
        ItemStackHandler handler = getInventoryHandler(stack);
        if(ModItems.FLOPPYDISK.equals(handler.getStackInSlot(0).getItem())) {
            return handler.getStackInSlot(0);
        }

        return ItemStack.EMPTY;
    }

    public static void setTransmitMode(ItemStack stack, TransmitMode mode) {
        if (mode != null) {
            stack.getOrCreateTag().putInt(TRANSMIT_MODE_TAG, mode.ordinal());
        } else if (stack.hasTag()) {
            stack.getTag().remove(TRANSMIT_MODE_TAG);
        }
    }

    public static TransmitMode getTransmitMode(ItemStack stack) {
        if (stackTagContainsKey(stack, TRANSMIT_MODE_TAG)) {
            return TransmitMode.fromInt(stack.getTag().getInt(TRANSMIT_MODE_TAG));
        }

        return TransmitMode.SELF;
    }
    
    public static void setTransmitId(ItemStack stack, UUID transmitId) {
        if (transmitId != null) {
            stack.getOrCreateTag().putUUID(TRANSMIT_ID_TAG, transmitId);
        } else if (stack.hasTag()) {
            stack.getTag().remove(TRANSMIT_ID_TAG);
        }
    }

    public static UUID getTransmitId(ItemStack stack) {
        if (stackTagContainsKey(stack, TRANSMIT_ID_TAG)) {
            return stack.getTag().getUUID(TRANSMIT_ID_TAG);
        }
        return null;
    }

    public static Boolean hasActiveFloppyDisk(ItemStack stack) {
        return !getActiveFloppyDiskStack(stack).isEmpty();
    }

    protected class TransmitterMenuProvider implements MenuProvider {
        @Override
        public AbstractContainerMenu createMenu(int p1, Inventory p2,  Player p3) {return null;}

        @Override
        public Component getDisplayName() {return null;}
    }

    protected static Boolean stackTagContainsKey(ItemStack stack, String tag) {
        return stack != null && stack.getTag() != null && stack.getTag().contains(tag);
    }
}

