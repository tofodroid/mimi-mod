package io.github.tofodroid.mods.mimi.common.item.legacycompat;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemFileCaster extends Item {
    public static final String REGISTRY_NAME = "filecaster";

    public ItemFileCaster(Properties props) {
        super(props.stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.mimi.removed.tooltip").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        // Client-side only
        if(worldIn != null && worldIn.isClientSide) {
            tooltip.add(Component.translatable("item.mimi.filecaster.tooltip"));
        }
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if(!worldIn.isClientSide) {
            playerIn.setItemInHand(handIn, ModItems.TRANSMITTER.getDefaultInstance());
            return new InteractionResultHolder<>(InteractionResult.CONSUME, playerIn.getItemInHand(handIn));
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, playerIn.getItemInHand(handIn));
    }
}
