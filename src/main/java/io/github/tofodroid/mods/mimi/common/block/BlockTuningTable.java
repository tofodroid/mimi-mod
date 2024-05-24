package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.container.ContainerTuningTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockTuningTable extends Block {
   public static final String REGISTRY_NAME = "tuningtable";
   private static final Component CONTAINER_TITLE = Component.translatable("container.tuning");
   
   protected BlockTuningTable() {
      super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
   }

   @Override
   public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
      if (worldIn.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         ((ServerPlayer) player).openMenu(this.getMenuProvider(state, worldIn, pos));
         return InteractionResult.CONSUME;
      }
   }

   
   public MenuProvider getMenuProvider(BlockState p_52240_, Level p_52241_, BlockPos p_52242_) {
      return new SimpleMenuProvider((p_52229_, p_52230_, p_52231_) -> {
         return new ContainerTuningTable(p_52229_, p_52230_, null);
      }, CONTAINER_TITLE);
   }
}