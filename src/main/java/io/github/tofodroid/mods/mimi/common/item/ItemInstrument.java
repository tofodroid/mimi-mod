package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;

public class ItemInstrument extends Item implements IDyeableItem {
    public final String REGISTRY_NAME;

    public static final String INVENTORY_TAG = "inventory";

    protected final Byte instrumentId;
    protected final Boolean dyeable;
    protected final Integer defaultColor;

    public ItemInstrument(String name, Byte instrumentId, Boolean dyeable, Integer defaultColor) {
        super(new Properties().tab(ModItems.ITEM_GROUP).stacksTo(1));
        this.REGISTRY_NAME = name;
        this.instrumentId = instrumentId;
        this.dyeable = dyeable;
        this.defaultColor = defaultColor;
    }

    @Override
    public Boolean isDyeable() {
        return this.dyeable;

    }

    @Override
    public Integer getDefaultColor() {
        return this.defaultColor;
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        if(washItem(context)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!worldIn.isClientSide) {
            NetworkHooks.openScreen((ServerPlayer) playerIn, generateContainerProvider(handIn), buffer -> {
                buffer.writeByte(this.instrumentId);
                buffer.writeBoolean(true);
                buffer.writeBoolean(InteractionHand.MAIN_HAND.equals(handIn));
            });
            return new InteractionResultHolder<>(InteractionResult.CONSUME, playerIn.getItemInHand(handIn));
        }

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
    }

    public MenuProvider generateContainerProvider(InteractionHand handIn) {
        return new InstrumentMenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int p1, Inventory p2,  Player p3) {
                return new ContainerInstrument(p1, p2, instrumentId, handIn);
            }
    
            @Override
            public Component getDisplayName() {return Component.literal("");}
        };
    }

    public Byte getInstrumentId() {
        return this.instrumentId;
    }

    public static Byte getInstrumentId(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemInstrument) {
            return ((ItemInstrument) stack.getItem()).getInstrumentId();
        }

        return null;
    }

    public static String getInstrumentName(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemInstrument) {
            return stack.getItem().getDescription().getString();
        }

        return null;
    }

    // Data Utils
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

    public static ItemStack getEntityHeldInstrumentStack(LivingEntity entity, InteractionHand handIn) {
        ItemStack heldStack = entity.getItemInHand(handIn);

        if(heldStack != null && heldStack.getItem() instanceof ItemInstrument) {
            return heldStack;
        }

        return null;
    }
    
    public static Boolean isEntityHoldingInstrument(LivingEntity entity) {
        return getEntityHeldInstrumentStack(entity, InteractionHand.MAIN_HAND) != null 
            || getEntityHeldInstrumentStack(entity, InteractionHand.OFF_HAND) != null;
    }

    public static Byte getEntityHeldInstrumentId(LivingEntity entity, InteractionHand handIn) {
        ItemStack instrumentStack = getEntityHeldInstrumentStack(entity, handIn);

        if(instrumentStack != null) {
            return ((ItemInstrument)instrumentStack.getItem()).getInstrumentId();
        }

        return null;
    }

    public static ItemStack getSwitchboardStack(ItemStack stack) {
        ItemStackHandler handler = getInventoryHandler(stack);
        if(ModItems.SWITCHBOARD.equals(handler.getStackInSlot(0).getItem())) {
            return handler.getStackInSlot(0);
        }

        return ItemStack.EMPTY;
    }
    
    public static Boolean shouldHandleMessage(ItemStack stack, UUID sender, Byte channel, Boolean publicTransmit) {
        ItemStack switchStack = getSwitchboardStack(stack);
        if(!switchStack.isEmpty()) {
            return ItemMidiSwitchboard.isChannelEnabled(switchStack, channel) && 
                ( 
                    (publicTransmit && ItemMidiSwitchboard.PUBLIC_SOURCE_ID.equals(ItemMidiSwitchboard.getMidiSource(switchStack))) 
                    || (sender != null && sender.equals(ItemMidiSwitchboard.getMidiSource(switchStack)))
                );
        }
        return false;
    }

    public static Boolean hasSwitchboard(ItemStack stack) {
        return !getSwitchboardStack(stack).isEmpty();
    }

    protected class InstrumentMenuProvider implements MenuProvider {
        @Override
        public AbstractContainerMenu createMenu(int p1, Inventory p2,  Player p3) {return null;}

        @Override
        public Component getDisplayName() {return null;}
    }
}
