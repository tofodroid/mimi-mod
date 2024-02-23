package io.github.tofodroid.mods.mimi.common.block.legacycompat;

import java.util.List;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockBroadcaster extends Block {
   public static final String REGISTRY_NAME = "broadcaster";
   
   public BlockBroadcaster() {
      super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable BlockGetter blockGetter, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal(""));
      tooltip.add(Component.translatableWithFallback("item.mimi.removed.tooltip", "REMOVED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
      tooltip.add(Component.translatableWithFallback("block.mimi.broadcaster.tooltip", "Place and right-click to convert me!"));
   }

   @Override
   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if(!worldIn.isClientSide) {
         worldIn.setBlock(pos, ModBlocks.TRANSMITTERBLOCK.defaultBlockState(), 2);
         ModBlocks.TRANSMITTERBLOCK.setPlacedBy(worldIn, pos, state, player, new ItemStack(ModBlocks.TRANSMITTERBLOCK.asItem()));
         return InteractionResult.SUCCESS;
      }
   
      return InteractionResult.PASS;
   }
}