package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;

public class ItemInstrument extends Item implements INamedContainerProvider {
    public static final String INVENTORY_TAG = "inventory";

    private final Byte instrumentId;

    public ItemInstrument(String name, final Byte instrumentId) {
        super(new Properties().group(ModItems.ITEM_GROUP).maxStackSize(1));
        this.setRegistryName(name);
        this.instrumentId = instrumentId;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) playerIn, this, buffer -> {
                buffer.writeByte(this.instrumentId);
                buffer.writeBoolean(true);
                buffer.writeBoolean(Hand.MAIN_HAND.equals(handIn));
            });
            return new ActionResult<>(ActionResultType.CONSUME, playerIn.getHeldItem(handIn));
        }

		return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        ItemStack mainStack = player.getHeldItemMainhand();

        if(mainStack.isEmpty() || !(mainStack.getItem() instanceof ItemInstrument)) {
		    return new ContainerInstrument(id, inv, instrumentId, Hand.OFF_HAND);
        } else {
		    return new ContainerInstrument(id, inv, instrumentId, Hand.MAIN_HAND);
        }
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(this.getTranslationKey());
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
            return stack.getItem().getName().getString();
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
                    || sender.equals(ItemMidiSwitchboard.getMidiSource(switchStack))
                );
        }
        return false;
    }

    public static Boolean hasSwitchboard(ItemStack stack) {
        return !getSwitchboardStack(stack).isEmpty();
    }
}
