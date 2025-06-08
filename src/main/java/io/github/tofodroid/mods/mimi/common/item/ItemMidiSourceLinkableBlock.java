package io.github.tofodroid.mods.mimi.common.item;

import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

public class ItemMidiSourceLinkableBlock extends BlockItem {
    public ItemMidiSourceLinkableBlock(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand handIn) {
        if(target instanceof Player && user.isCrouching()) {
            if(!user.level().isClientSide) {
                MidiNbtDataUtils.setMidiSource(stack, target.getUUID(), target.getName().getString());
                user.setItemInHand(handIn, stack);
                Component message = Component.literal("Linked ").append(stack.getHoverName()).append(Component.literal(" to ")).append(target.getName());
                user.displayClientMessage(message, true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        Block block = level.getBlockState(pos).getBlock();
        return block.equals(ModBlocks.TRANSMITTERBLOCK) || block.equals(ModBlocks.RELAY);
    }
}
