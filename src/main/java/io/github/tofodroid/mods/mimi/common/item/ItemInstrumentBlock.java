package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemInstrumentBlock extends BlockItem implements IInstrumentItem {
    protected final String REGISTRY_NAME;
    protected final BlockInstrument instrumentBlock;

    public ItemInstrumentBlock(BlockInstrument blockIn, Properties props, String name) {
        super(blockIn, props);
        this.REGISTRY_NAME = name;
        this.instrumentBlock = blockIn;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        MidiNbtDataUtils.appendSettingsTooltip(stack, tooltip);
    }

    @Override
    public Byte getInstrumentId() {
        return this.instrumentBlock.getInstrumentId();
    }

    @Override
    public Integer getDefaultColor() {
        return this.instrumentBlock.getDefaultColor();
    }

    @Override
    public Integer getDefaultChannels() {
        return this.instrumentBlock.getDefaultChannels();
    }

    @Override
    @SuppressWarnings("resource")
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand handIn) {
        if(!user.level().isClientSide) {
            if(target instanceof Player) {
                MidiNbtDataUtils.setMidiSource(stack, target.getUUID(), target.getName().getString());
                user.setItemInHand(handIn, stack);
                user.displayClientMessage(Component.literal("Linked to " + target.getName().getString()), true);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public String getRegistryName() {
        return this.REGISTRY_NAME;
    }
}
