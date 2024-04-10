package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;

public class ItemInstrumentHandheld extends Item implements IInstrumentItem {
    protected final String REGISTRY_NAME;
    protected final Byte instrumentId;
    protected final Integer defaultChannels;
    protected final Integer defaultColor;
    protected final Boolean colorable;

    public ItemInstrumentHandheld(Properties props, Byte instrumentId) {
        super(props.stacksTo(1));
        InstrumentSpec spec = InstrumentConfig.getBydId(instrumentId);
        this.REGISTRY_NAME = spec.registryName;
        this.defaultChannels = MidiNbtDataUtils.getDefaultChannelsForBank(spec.midiBankNumber);
        this.instrumentId = instrumentId;
        this.colorable = spec.isColorable();
        this.defaultColor = this.colorable ? spec.defaultColor() : null;
    }

    public ItemInstrumentHandheld(Properties props, InstrumentSpec spec) {
        super(props.stacksTo(1));
        this.REGISTRY_NAME = spec.registryName;
        this.defaultChannels = MidiNbtDataUtils.getDefaultChannelsForBank(spec.midiBankNumber);
        this.instrumentId = spec.instrumentId;
        this.colorable = spec.isColorable();
        this.defaultColor = this.colorable ? spec.defaultColor() : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // Client-side only
        if(worldIn != null && worldIn.isClientSide) {
            MidiNbtDataUtils.appendSettingsTooltip(stack, tooltip);
        }
    }

    @Override
    public Boolean isColorable() {
        return this.colorable;
    }

    @Override
    public Integer getDefaultColor() {
        return this.defaultColor;
    }

    @Override
    public Byte getInstrumentId() {
        return this.instrumentId;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(washItem(context)) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
    
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.OFFHAND;
    }

    @Override
    @SuppressWarnings({"resource", "deprecation"})
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand handIn) {
        if(target instanceof Player) {
            if(!user.getLevel().isClientSide) {
                MidiNbtDataUtils.setMidiSource(stack, target.getUUID(), target.getName().getString());
                user.setItemInHand(handIn, stack);
                user.displayClientMessage(Component.literal("Linked to " + target.getName().getString()), true);
                ServerNoteConsumerManager.handlePacket(MidiNotePacket.createAllNotesOffPacket(getInstrumentId(), user.getUUID(), user.getOnPos(), handIn), null, (ServerLevel)user.getLevel());
            }
             return InteractionResult.CONSUME;
        } else if(target instanceof Mob) {
            if(!user.getLevel().isClientSide && ConfigProxy.getAllowedInstrumentMobs().contains(target.getType().builtInRegistryHolder().key().location().toString()) && ((Mob)target).equipItemIfPossible(stack)) {
                user.setItemInHand(handIn, ItemStack.EMPTY);
                target.playSound(SoundEvents.DONKEY_CHEST, 1.0F, 1.0F);
                ServerNoteConsumerManager.handlePacket(MidiNotePacket.createAllNotesOffPacket(getInstrumentId(), user.getUUID(), user.getOnPos(), handIn), null, (ServerLevel)user.getLevel());
            }
            return InteractionResult.CONSUME;
        }
        
        return InteractionResult.PASS;
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if(worldIn.isClientSide && !playerIn.isCrouching()) {
            ClientGuiWrapper.openInstrumentGui(worldIn, playerIn, handIn, playerIn.getItemInHand(handIn));
		    return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
        }

		return new InteractionResultHolder<>(InteractionResult.PASS, playerIn.getItemInHand(handIn));
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
        if(tag == null) return;

        if(tag.contains("inventory")) {
            ListTag listtag = tag.getCompound("inventory").getList("Items", 10);

            if(listtag != null && listtag.size() > 0) {
                for(int i = 0; i < listtag.size(); ++i) {
                    CompoundTag stackTag = listtag.getCompound(i);
                    String itemId = stackTag.getString("id");

                    if(itemId.equalsIgnoreCase("mimi:switchboard") && stackTag.contains("tag", 10)) {
                        tag = tag.merge(MidiNbtDataUtils.convertSwitchboardToDataTag(stackTag.getCompound("tag")));
                        tag.remove("inventory");
                    }
                }
            }
        }
    }

    public static ItemStack getEntityHeldInstrumentStack(LivingEntity entity, InteractionHand handIn) {
        ItemStack heldStack = entity.getItemInHand(handIn);

        if(heldStack != null && heldStack.getItem() instanceof ItemInstrumentHandheld) {
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
            return ((ItemInstrumentHandheld)instrumentStack.getItem()).getInstrumentId();
        }

        return null;
    }

    @Override
    public Integer getDefaultChannels() {
        return this.defaultChannels;
    }

    @Override
    public String getRegistryName() {
        return this.REGISTRY_NAME;
    }
}
